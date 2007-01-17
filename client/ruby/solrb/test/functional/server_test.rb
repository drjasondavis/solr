# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require 'test/unit'
require 'solr'

class BadRequest < Solr::Request::Standard
  def response_format
    :invalid
  end
end

class ServerTest < Test::Unit::TestCase
  include Solr

  def setup
    @connection = Connection.new("http://localhost:8888/solr", :autocommit => :on)
    clean
  end
 
  def test_full_lifecycle
    # make sure autocommit is on
    assert @connection.autocommit

    # make sure this doc isn't there to begin with
    @connection.delete(123456)

    # add it
    @connection.add(:id => 123456, :text => 'Borges')

    # look for it
    response = @connection.query('Borges')
    assert_equal 1, response.total_hits
    assert_equal '123456', response.hits[0]['id']

    # delete it
    @connection.delete(123456)

    # make sure it's gone
    response = @connection.query('Borges')
    assert_equal 0, response.total_hits
  end 

  def test_bad_connection
    conn = Solr::Connection.new 'http://localhost:9999/invalid'
    assert_raise(Errno::ECONNREFUSED) do
      conn.send(Solr::Request::Ping.new)
    end
  end
  
  def test_bad_url
    conn = Solr::Connection.new 'http://localhost:8888/invalid'
    assert_raise(Net::HTTPServerException) do
      conn.send(Solr::Request::Ping.new)
    end
  end
  
  def test_commit
    response = @connection.send(Solr::Request::Commit.new)
    assert_equal "<result status=\"0\"></result>", response.raw_response
  end
  
  def test_ping
    assert_equal true, @connection.ping
  end

  def test_delete_with_query
    assert_equal true, @connection.delete_by_query('[* TO *]')
  end

  def test_ping_with_bad_server
    conn = Solr::Connection.new 'http://localhost:8888/invalid'
    assert_equal false, conn.ping
  end
  
  def test_invalid_response_format
    request = BadRequest.new(:query => "solr")
    assert_raise(Solr::Exception) do
      @connection.send(request)
    end
  end
  
  def test_escaping
    doc = Solr::Document.new :id => 47, :ruby_text => 'puts "ouch!"'
    @connection.send(Solr::Request::AddDocument.new(doc))
    @connection.commit
    
    request = Solr::Request::Standard.new :query => 'ouch'
    result = @connection.send(request)
    
    assert_match /puts/, result.raw_response
  end

  def test_add_document
    doc = {:id => 999, :text => 'hi there!'}
    request = Solr::Request::AddDocument.new(doc)
    response = @connection.send(request)
    assert response.status_code == '0'
  end

  def test_update
    @connection.update(:id => 999, :text => 'update test')
  end

  def test_no_such_field
    doc = {:id => 999, :crap => 'foo'}
    request = Solr::Request::AddDocument.new(doc)
    response = @connection.send(request)
    assert_equal false, response.ok? 
    assert_equal "ERROR:unknown field 'crap'", response.status_message
  end

  # wipe the index clean
  def clean
    @connection.delete_by_query('[* TO *]')
  end

end
