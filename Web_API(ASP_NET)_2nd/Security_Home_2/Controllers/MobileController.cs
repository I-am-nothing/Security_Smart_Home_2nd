using Security_Home_2.Models;
using Security_Home_2.Models.Encryption;
using Security_Home_2.Models.Model;
using Security_Home_2.Models.Response;
using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace Security_Home_2.Controllers
{
    public class MobileController : ApiController
    {
        Connection cn = new Connection();

        [Route("mobile/register")]
        [HttpPost]
        public HttpResponseMessage register([FromBody] Mobile encryptionMobile)
        {
            Mobile mobile = SecurityHomeEncryption.decrypt<Mobile>(encryptionMobile);
            String sqlStr = "SELECT Account FROM Account.Main WHERE Account = '" + mobile.Account + "'";

            if (cn.getData(sqlStr).Rows.Count == 0)
            {
                Trace.WriteLine(mobile.DeviceId);
                Trace.WriteLine(mobile.Account);
                Trace.WriteLine(mobile.Password);
                Trace.WriteLine(mobile.Name);

                String sqlStr2 = @"DECLARE @ID varchar(50) = newID()
                                   DECLARE @Device_ID varchar(50) = '" + mobile.DeviceId + @"'
                                   DECLARE @Account varchar(50) = '" + mobile.Account + @"'
                                   DECLARE @Password varchar(20) = '" + mobile.Password + @"'
                                   DECLARE @Name nvarchar(20) = N'" + mobile.Name + @"'
                                   INSERT INTO Account.Main VALUES(@ID, @Account, @Password, '0', '0', '1')
                                   INSERT INTO Account.Device VALUES(@Device_ID, @ID, '1')
                                   INSERT INTO Account.Profile VALUES(@ID, @Name, NULL, NULL, NULL, NULL)
                                   SELECT @ID Account_ID";

                return Request.CreateResponse(HttpStatusCode.OK, new Success("1", SecurityHomeEncryption.encrypt(cn.getData(sqlStr2))));
            }
            
            return Request.CreateResponse(HttpStatusCode.OK, new Success("0", "This Account is already registered!"));
        }

        [Route("mobile/login")]
        [HttpPost]
        public HttpResponseMessage logIn([FromBody] Mobile encryptionMobile)
        {
            Mobile mobile = SecurityHomeEncryption.decrypt<Mobile>(encryptionMobile);
            String sqlStr = "SELECT Account_ID FROM Account.Main WHERE Account = '" + mobile.Account + "' AND Password = '" + mobile.Password + "'";
            DataTable dt = cn.getData(sqlStr);

            if (dt.Rows.Count == 0)
            {
                return Request.CreateResponse(HttpStatusCode.OK, new Success("0", "Account or password incorrect"));
            }

            return Request.CreateResponse(HttpStatusCode.OK, new Success("1", SecurityHomeEncryption.encrypt(dt)));
        }

        [Route("mobile/device_login")]
        [HttpPost]
        public HttpResponseMessage deviceLogIn([FromBody] Mobile encryptionMobile)
        {
            Mobile mobile = SecurityHomeEncryption.decrypt<Mobile>(encryptionMobile);
            String sqlStr = "SELECT Status FROM Account.Device WHERE Account_ID = '" + mobile.AccountId + "' AND Device_ID = '" + mobile.DeviceId.ToString() + "'";
            DataTable dt = cn.getData(sqlStr);

            if (dt.Rows.Count == 0)
            {
                String sqlStr2 = "INSERT INTO Account.Device VALUES ('" + mobile.DeviceId + "', '" + mobile.AccountId + "', '0')";
                cn.getData(sqlStr2);

                return Request.CreateResponse(HttpStatusCode.OK, new Success("0", "This device is not registed"));
            }

            return Request.CreateResponse(HttpStatusCode.OK, new Success("1", "Device Login Succeed"));
        }
    }
}
