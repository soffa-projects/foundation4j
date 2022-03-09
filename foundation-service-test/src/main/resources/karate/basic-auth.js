function fn(creds) {
    var helper = Java.type('dev.soffa.foundation.test.KarateTestUtil');
    return helper.basicAuth(creds.username, creds.password);
}
