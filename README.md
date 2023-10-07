# kotlin-rest-client

This is a very simple implementation of and REST-client based on ktor.


## Example Usage

```kotlin
    val client = RestClient("https://myapi.example.com/v1")
    val response = client.get("/users")
```