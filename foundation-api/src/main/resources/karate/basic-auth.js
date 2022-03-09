function fn(creds) {
    var helper = Java.type('dev.soffa.foundation.commons.http.HttpUtil');
    return helper.createBasicAuthorization(creds.username, creds.password);
}
