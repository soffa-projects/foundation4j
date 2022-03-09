function fn() {
    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);
    // karate.configure('retry', { count: 2, interval: 1000 });
    var config = JSON.parse(karate.properties['config'] || "{}")
    if (!config.baseUrl) {
        config.baseUrl = 'http://mockmvc';
    }
    return config;
}
