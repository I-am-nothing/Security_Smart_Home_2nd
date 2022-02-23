using System;
using System.Collections.Generic;
using System.Linq;
using System.Web.Http;
using Security_Home_2.Models;
using System.Threading;
using WebSocketSharp;
using WebSocketSharp.Server;
using System.Threading.Tasks;

namespace Security_Home_2
{
    public static class WebApiConfig
    {
        public static void Register(HttpConfiguration config)
        {
            // Web API configuration and services
            Connection cn = new Connection();
            cn.connect();
            // Web API routes
            config.MapHttpAttributeRoutes();

            config.Routes.MapHttpRoute(
                name: "DefaultApi",
                routeTemplate: "api/{controller}/{id}",
                defaults: new { id = RouteParameter.Optional }
            );
        }
    }
}
