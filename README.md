# Search Engine Updater Service

### Basic information

* Java 21
* Spring Boot
* Gradle
* H2

### Building & Deployment
The service can be built with the following command:
`gradlew clean build`

Run with debug:
`gradlew bootRun --debug-jvm`

Or deploy using docker (after gradle build). Build an image and run container in 1 step from the folder "docker":
`docker-compose up --build`

### Usage
Possible input operations:
- Upsert a product
- Upsert an offer
- Delete a product
- Delete an offer

Possible output operations:
- Upsert a searchable product document
- Delete a searchable product document

POST request of format:
```
{
    "operation": "UPSERT_OFFER"
    "offerId": "offerA",
    "relatedProductId": "productX",
    "offerName": "buy offer A"
}
```
```
{
    "operation": "UPSERT_PRODUCT"
    "productId": "productX"
    "productName": "great product X"
}
```
```
{
    "operation": "DELETE_PRODUCT"
    "productId": "productX"
}
```
```
{
    "operation": "DELETE_OFFER"
    "offerId": "offerA"
}
```

to the endpoint:
`<host>:8090/api/ingest`

Responses:
- no changes
```
[]
```
- Only update searchable product
```
[
	{
		"productId": "productX",
		"productName": "greatProductX",
		"offerNames": [
			"buy offer A"
		],
		"operationType": "UPSERT_SEARCHABLE_PRODUCT"
	}
]
```
- delete one and update another searchable product
```
[
	{
		"productId": "productX",
		"operationType": "DELETE_SEARCHABLE_PRODUCT"
	},
	{
		"productId": "productY",
		"productName": "awesome product Y",
		"offerNames": [
			"buy offer A again"
		],
		"operationType": "UPSERT_SEARCHABLE_PRODUCT"
	}
]
```
