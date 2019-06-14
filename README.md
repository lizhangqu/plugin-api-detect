### api detect plugin

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "io.github.lizhangqu:plugin-api-detect:1.0.5"
    }
}

apply plugin: 'api.detect'
api {
    detectPattern "io.github.sample.*"
    detectPattern "org.apache.*"
}
```

