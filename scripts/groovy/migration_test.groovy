import static groovyx.net.http.ContentType.*
import groovyx.net.http.RESTClient

// This script tests known profile data has been migrated correctly.
// Before migration create a profile item for the following data item:
// /data/​business/​energy/​stationaryCombustion/​defra/​energy/drill?fuel=natural%20gas&basis=gross
// energy = 1000
// Check the emission amounts below are correct.

/****************************************/

host = 'http://platform-qa.amee.com'
username = 'ameetest2'
password = '8b3pf87m'
profileUid = 'TSBXNYFLNUD0'
profileItemUid = 'X7ZHMOEVU6ZF'

/****************************************/

auth = 'Basic ' + (username + ':' + password).bytes.encodeBase64().toString()
profileItemPath = "profiles/${profileUid}/business/energy/stationaryCombustion/defra/energy/${profileItemUid}"
amounts = [
    'totalDirectCO2e': 185.23000000000002,
    'lifeCycleCO2e': 203.22,
    'CO2': 184.85,
    'nitrousOxideCO2e': 0.11,
    'methaneCO2e': 0.27,
    'indirectCO2e': 17.99]


@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1')
def getRESTClient() {
    def client = new RESTClient(host)
//    client.requestContentType = JSON
    client.contentType = JSON

    // Can't use the built in auth handling as we don't send the WWW-Authenticate header in v2
    //client.auth.basic username, password
    client.headers.Authorization = auth
    return client
}

def client = getRESTClient()

println "Testing ${host}"
println "Profile UID: ${profileUid}"
println "Profile ITEM UID: ${profileItemUid}"
println "Path: ${profileItemPath}"

// Ensure that the Profile created still exists
print "Checking profile exists... "
def response = client.get(path: "profiles")
assert response.data.profiles.find { it.uid == profileUid } != null
println "ok"

// Ensure that the Profile Item still exists
print "Checking profile item exists... "
response = client.get(path: profileItemPath)
assert response.data.profileItem.uid == profileItemUid
println "ok"

// Test that all of the emissions for the Profile Item are unchanged.
print "Checking emissions are correct... "
amounts.each { type, value  ->
    assert response.data.profileItem.amounts.amount.find { it.type == type }.value == value
}
println "ok"

// Ensure that the Profile Item can be updated
print "Checking profile item can be updated... "
response = client.put(path: profileItemPath,
    body: ['name': 'test', 'representation': 'full'],
    requestContentType: URLENC)
assert response.data.profileItem.name == 'test'
println "ok"

// Ensure that the Profile Item Value can be updated
print "Checking profile item value can be updated... "
response = client.put(path: profileItemPath,
    body: ['energy': '2000', 'representation': 'full'],
    requestContentType: URLENC)
println "ok"

// Ensure that all of the emissions for the Profile Item are now double what they were before the update.
print "Checking emissions have been updated... "
amounts.each { type, value  ->
    assert response.data.profileItem.amounts.amount.find { it.type == type }.value == value * 2
}
println "ok"

// Ensure that it’s possible to delete the Profile Item
print "Deleting profile item... "
response = client.delete(path: profileItemPath)
assert response.status == 200

// Check it has really been deleted
try {
    client.get(path: profileItemPath)
    assert false, 'Expected exception'
} catch (ex) {
    assert ex.response.status == 404
}
println "ok"

// Ensure that it’s possible to delete the Profile
print "Deleting profile... "
response = client.delete(path: "profiles/${profileUid}")
assert response.status == 200

// Check it has really been deleted
try {
    client.get(path: "profiles/${profileUid}")
    assert false, 'Expected exception'
} catch (ex) {
    assert ex.response.status == 404
}
println "ok"
println 'Migration test completed successfully'