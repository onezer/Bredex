# Used car ad handling back-end application

## Features
### User signup
At the publicly available `/auth/signup` API endpoint the client
can make a user registration with a `Http POST` request.
The request body must be in the following JSON format:

```json
{
  "username":"username",
  "password":"Password1",
  "email":"John@gmail.com"
}
```
The username must be between 1 and 50 characters and only contain letters, numbers
and the `-` and`_` characters.
The password must be at least 8 characters long and contain uppercase, lowercase
letters and numbers.
The e-mail address must be in a valid e-mail format, that matches the following regex:
`^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$`
Both the username and e-mail address must be unique.

### User login
At the publicly available `/auth/login` API endpoint the client
can log in with a `Http POST` request.
The request body must be in the following JSON format:
```json
{
    "username":"username",
    "password":"Password2"
}
```
With a successful authentication the client receives two JWT tokens, an access token and 
a refresh one in the following format:
```json
{
  "refresh-token": "<token>",
  "access-token": "<token>"
}
```
It is important to note that the refresh token is only usable for the `/auth/refresh-token`
endpoint with a `Http GET` request.
The access token is valid for 1 hour, the refresh token is valid for 2 hours.

### User token refresh
At the `/auth/refresh-token` endpoint with a valid refresh token the client can get new tokens similarly 
to the login one.
But with only a valid, not expired refresh token. After getting new tokens, the old ones become unusable.
### User logout
The client can log out with a valid access token at the `/auth/logout` endpoint with a `Http POST` request. Logging out
makes the user's tokens invalid.
### Ad posting
With a valid access token the user can post an ad for a car at the `/ad`  with a `Http POST` request.
The request body must contain the following JSON format:
```json
{
  "brand": "Toyota",
  "model": "Corolla",
  "description": "A reliable car with great fuel efficiency.",
  "price": "15000"
}
```
### Field requirements:
 - **brand**: must be no more than 20 characters
 - **model**: must be no more than 20 characters
 - **description**: must be no more than 200 characters
 - **price**: must only contain numbers and be maximum 10 digits

After a successful posting the client gets back the id of the ad.

### Ad searching
A client can search for ads at the `/ad/search` endpoint with a `Http GET` request. The request body must contain the 
following JSON:
```json
{
    "brand":"Toyota",
    "model":"Corolla",
    "price":"100000"
}
```
With all the fields being optional, the following formats are also valid:
```json
{
    "brand":"Toyota",
    "model":"Corolla"
}
```
```json
{
    "brand":"Toyota"
}
```
```json
{}
```
The search works with partial `brand` and `model` matches. The `price` narrows it down to with a less or equal relation
to the cars' price.
With the search the client receives a list of the matching results.
The results are the URLs in the following format:
```json
[
    "http://localhost:9000/ad/1",
    "http://localhost:9000/ad/2",
    "http://localhost:9000/ad/3",
    "http://localhost:9000/ad/4",
    "http://localhost:9000/ad/5",
    "http://localhost:9000/ad/6",
    "http://localhost:9000/ad/7",
    "http://localhost:9000/ad/8",
    "http://localhost:9000/ad/9",
    "http://localhost:9000/ad/10"
]
```
The client can access the individual ads with a `Http GET` on the URLs above.
The ad information is in the following format:
```json
{
    "id": 2,
    "brand": "Toyota",
    "model": "Camry",
    "description": "Spacious and comfortable car with excellent mileage.",
    "price": 850000,
    "email": "jane@gmail.com"
}
```
### Ad deleting
A user can delete their posted ads with the `/ad/{id}` endpoint with an `Http DELETE` request.

### Other
The application logs in the database the user logins and logouts with a timestamp. For extra security the app checks 
at the token validation if the user's last action was not a logout one.
The application also stores the issued tokens, so when a user logs out or gets new tokens, it deletes the old ones from
the database. It also checks at token validation if the token is stored in the database.
Also, the application starts with some initial data in the database.

## Configuration
The app can be configured in the `application.properties` file.

The following can be configured:
 - `security.jwt.secret-key`: A base64-encoded HMAC hash string of 256 bits used for signing tokens.
 - `security.jwt.access-token-expiration`: Specifies the access token's expiration time in milliseconds. Default: 1 hour.
 - `security.jwt.refresh-token-expiration`: Specifies the refresh token's expiration time in milliseconds. Default: 2 hours.
 - `spring.datasource.url`: The database connection URL. The default is an in-memory H2 database. Ensure the selected database supports sequential ID generation.
 - `spring.datasource.driverClassName`: The driver class name for the database.
 - `service.base-url`: The base URL where the application can be accessed.

## Features to be implemented to be a complete application
### User account deletion
Users should be able to delete their accounts and all their associated private data.
### User roles
There should be users with moderator/admin privilege that can moderate other users if they abuse the application some ways.
Or posting inappropriate ads.
### Ads storing pictures of cars
Usually people want to see the car they intend to buy.
### More search options
There should be more details about cars (e.g. age, color) that should also be searchable.
### User account recovery
It's not uncommon for people to forget the username/password. There should be options
for password change (e.g. through e-mail or phone).
### Two-factor authentication
A username/password combination is still not the most secure way of authenticating. There should be an option for the user
to use two-factor authentication (eg. e-mail, text message, 3rd party authenticator app).
### User account modification
Users should be able to change their passwords and e-mail addresses.
### Ad modification
Users should be able to modify their existing ads.