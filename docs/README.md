Keeping your service API documentation up-to-date. Doing this provides
a number of benefits, including:

1. Ensuring that your API contract is well known and defined.
2. Ensuring your API consumers don't hate you.

### Swagger Editor

You can use the Swagger Editor to edit and test your `swagger.yaml` file.
Copy and paste your API specification or use `File -> Import File` and `File -> Download YAML`.

```
docker run -p 8005:8080 swaggerapi/swagger-editor
open http://localhost:8005/
```
