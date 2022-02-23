using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.WebSockets;
using System.Threading;
using System.Threading.Tasks;
using System.Web;
using System.Web.Http;
using System.Web.WebSockets;
using Microsoft.Web.WebSockets;
using Security_Home_2.Models;
using Security_Home_2.Models.Model;
using Security_Home_2.Models.Encryption;
using Newtonsoft.Json;
using System.Data.Sql;
using System.Data.SqlClient;
using System.Data;
using TableDependency.SqlClient;
using TableDependency.SqlClient.Base.EventArgs;
using System.Diagnostics;

namespace Security_Home_2.Controllers
{
    public class WebSocketHomeDeviceController : ApiController
    {
        [Route("web_socket/home_device")]
        [HttpGet]
        public HttpResponseMessage Get()
        {
            var currentContext = HttpContext.Current;

            if (currentContext.IsWebSocketRequest || currentContext.IsWebSocketRequestUpgrading)
            {
                currentContext.AcceptWebSocketRequest(ProcessWebsocketSession);
            }

            return Request.CreateResponse(HttpStatusCode.SwitchingProtocols);
        }

        private Task ProcessWebsocketSession(AspNetWebSocketContext context)
        {
            var handler = new MyWebSocketHandler(context);
            var processTask = handler.ProcessWebSocketRequestAsync(context);
            return processTask;
        }

        public class MyWebSocketHandler : WebSocketHandler
        {
            private static WebSocketCollection clients = new WebSocketCollection();
            private Connection cn = new Connection();
            private AspNetWebSocketContext context;
            private HomeDevice reciver;
            private ConnectSocket cs;

            public MyWebSocketHandler(AspNetWebSocketContext context)
            {
                this.context = context;
            }

            public override void OnOpen()
            {
                clients.Add(this);
            }

            public override void OnMessage(String message)
            {
                Send("Connecting...");

                reciver = JsonConvert.DeserializeObject<HomeDevice>(message);
                cs = cn.checkHomeDeviceSocket(reciver);

                if (cs == ConnectSocket.AlreadyConnect)
                {
                    Send("This device is already connected!");
                    Thread.Sleep(500);
                    Close();
                }
                else if(cs == ConnectSocket.NotFindDevice)
                {
                    Send("This device is not found");
                    Thread.Sleep(500);
                    Close();
                }
                else if(cs == ConnectSocket.Success)
                {
                    Send("Security_Home_2 web socket connect succeed!");

                    String sqlStr = "SELECT Command_ID FROM dbo.Notification WHERE Reciver_ID = '" + reciver.deviceId + "' AND Status = '0'";
                    Send(JsonConvert.SerializeObject(SecurityHomeEncryption.encrypt(cn.getData(sqlStr))));

                    SqlTableDependency<Notification> tableDependency = new SqlTableDependency<Notification>(cn.getConnectionString(), "Notification", "dbo");

                    tableDependency.OnChanged += TableDependency_Changed;
                    tableDependency.OnError += TableDependencyj_OnError;
                    tableDependency.Start();
                }
            }

            private void TableDependencyj_OnError(object sender, ErrorEventArgs e)
            {
                throw e.Error;
            }

            private void TableDependency_Changed(object sender, RecordChangedEventArgs<Notification> e)
            {
                if (e.ChangeType == TableDependency.SqlClient.Base.Enums.ChangeType.Insert)
                {
                    ObjectNotification data = new ObjectNotification(e.Entity);

                    if (e.Entity.Reciver_ID == (String)reciver.deviceId)
                    {
                        Send("[" + JsonConvert.SerializeObject(SecurityHomeEncryption.encrypt<ObjectNotification>(data)) + "]");
                    }
                }
            }

            public override void OnClose()
            {
                if(cs == ConnectSocket.Success)
                {
                    cn.closeHomeDeviceSocket(reciver);
                }
                clients.Remove(this);

                base.OnClose();
            }

            public override void OnError()
            {
                Send("Error");
                base.OnError();
            }
        }
    }
}
