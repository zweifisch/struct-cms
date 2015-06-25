(ns cms.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [cms.routes.home :refer [home-routes]]
            [cms.routes.template :refer [template-routes]]
            [cms.routes.namespace :refer [namespace-routes]]
            [cms.routes.user :refer [user-routes]]
            [cms.routes.article :refer [article-routes]]
            [cms.routes.api :refer [api-routes]]
            [cms.middleware :as middleware]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [aussen.core :refer [env]]
            [clojure.tools.nrepl.server :as nrepl]))

(defonce nrepl-server (atom nil))

(defroutes base-routes
           (route/resources "/")
           (route/not-found "Not Found"))

(defn start-nrepl
  "Start a network repl for debugging when the :repl-port is set in the environment."
  []
  (when-let [port (env :repl-port)]
    (try
      (reset! nrepl-server (nrepl/start-server :port port))
      (timbre/info "nREPL server started on port" port)
      (catch Throwable t
        (timbre/error "failed to start nREPL" t)))))

(defn stop-nrepl []
  (when-let [server @nrepl-server]
    (nrepl/stop-server server)))

(defn init
  "init will be called once when
  app is deployed as a servlet on
  an app server such as Tomcat
  put any initialization code here"
  []

  (timbre/set-config!
   [:appenders :rotor]
   {:min-level             (if (env :dev) :trace :info)
    :enabled?              true
    :async?                false ; should be always false for rotor
    :max-message-per-msecs nil
    :fn                    rotor/appender-fn})

  (timbre/set-config!
   [:shared-appender-config :rotor]
   {:path "cms.log" :max-size (* 512 1024) :backlog 10})

  (if (env :dev) (parser/cache-off!))
  (start-nrepl)
  (timbre/info "\n-=[ cms started successfully"
               (when (env :dev) "using the development profile") "]=-"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "cms is shutting down...")
  (stop-nrepl)
  (timbre/info "shutdown complete!"))

(def app
  (-> (routes
       home-routes
       template-routes
       namespace-routes
       user-routes
       article-routes
       api-routes
       base-routes)
      middleware/wrap-base))
