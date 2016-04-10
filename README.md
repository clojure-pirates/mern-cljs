# MERN-Cljs

What is it?

- Stack with MongoDB Express React Node.js and ClojureScript
- Build React backed web apps in Clojure (reagent + kioo)
- Login with a social network account via Passport.js
- Async worker via RabbitMQ

For now, this repo serves to share the knowledge on how to achieve above
and I don't intend to release a library or framework :)

## What you need to install

1. Node.js
2. MongoDB
3. Leiningen

## How to run locally

Run MongoDB

```
brew services start mongodb
```

Run the example API server

```
cd example/api 
lein compile
lein run
```

Run the web app 

```
cd example/www
lein compile
lein run
```

Point the browser to `http://localhost:1337/profile`
(API server is served on port 5000 by default.)

## Configuration

See example/common/src/common/config.cljs 

## About API Authentication process

Frontend app tries to fetch `me` resource. If it fails, it will redirect to
Social auth. WWW backend authenticates via Passport.js and write information
on MongoDB with a one time API token. Token is passed on to the frontend and
it is used to API auth. In the example app, it will be redirected to profile
page to fetch `me` resource with proper authentication.

## Async tasks

If you run async task, install and run RabbitMQ first:

```
brew install rabbitmq
brew services start rabbitmq
```

Then uncomment `create-amqp-conn` around Ln. 39 of `example/api/src/api/core.cljs`:

```
...
(defn server [success]
; Activate the next line if you want to run async task 
; (create-amqp-conn amqp-endpoint)
...
```

Recompile and run api instance:

```
cd example/api
lein compile
lein run
```

Start worker instance:

```
cd example/worker
lein compile
lein run
```

Activate RabbitMQ management dashboard plugin:

```
rabbitmq-plugins enable rabbitmq_management
```

Point your browser to http://localhost:15672/#/connections to see the connections.
There should be one from api instance and one from worker.

Point your browser to http://localhost:5000/task to trigger a trivial task.
Check the console of worker instance to see if the task was received. This should
look like:

```
 [*] Waiting for messages. To exit press CTRL+C
 [x] Received message hello
```

## Issues

- Google Auth requires consent everytime the page reloads

## TODOs

- Add Twitter auth

## References

- [Easy Node Authentication: Setup and Local | Scotch](https://scotch.io/tutorials/easy-node-authentication-setup-and-local)
