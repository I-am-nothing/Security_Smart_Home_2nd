using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using System.Web.Http;
using Security_Home_2.Models;
using Security_Home_2.Models.Encryption;
using Security_Home_2.Models.Model;
using Security_Home_2.Models.Response;

namespace Security_Home_2.Controllers
{
    public class HomeDeviceController : ApiController
    {
        Connection cn = new Connection();

        [Route("home_device/new_device")]
        [HttpPost]
        public HttpResponseMessage newDevice([FromBody] HomeDevice encryptionHomeDevice)
        {
            HomeDevice homeDevice = SecurityHomeEncryption.decrypt<HomeDevice>(encryptionHomeDevice);
            String sqlStr = "SELECT Device_ID FROM Home.Device WHERE Chip_ID = '" + homeDevice.chipId + "' AND Mac_Address = '" + homeDevice.macAddress + "'";

            if (cn.getData(sqlStr).Rows.Count == 0)
            {
                String sqlStr2 = @"DECLARE @ID varchar(50) = newID()
                                   DECLARE @Type_ID varchar(50) = '" + homeDevice.typeId + @"'
                                   DECLARE @Chip_ID varchar(10) = '" + homeDevice.chipId + @"'
                                   DECLARE @Mac_Address varchar(20) = '" + homeDevice.macAddress + @"'
                                   DECLARE @Name nvarchar(20)
                                   DECLARE @Detail nvarchar(MAX)
                                   SELECT @Name = Name, @Detail = Detail FROM Home.Device_Type Where Type_ID = @Type_ID
                                   INSERT INTO Home.Device VALUES(@ID, @Type_ID, @Chip_ID, @Mac_Address, @Name, @Detail)
                                   SELECT @ID Device_ID, @Name Name, @Detail Detail";

                return Request.CreateResponse(HttpStatusCode.OK, new Success("1", SecurityHomeEncryption.encrypt(cn.getData(sqlStr2))));
            }

            return Request.CreateResponse(HttpStatusCode.BadRequest, SecurityHomeEncryption.encrypt("This Device is already registered!"));
        }

        [Route("home_device/socket/reconnect")]
        [HttpPost]
        public HttpResponseMessage reconnect([FromBody] HomeDevice encryptionHomeDevice)
        {
            HomeDevice homeDevice = SecurityHomeEncryption.decrypt<HomeDevice>(encryptionHomeDevice);

            String sqlStr = "SELECT Device_ID From Home.Device WHERE Chip_ID = '" + homeDevice.chipId + "' AND Mac_Address = '" + homeDevice.macAddress + "' AND Device_ID = '" + homeDevice.deviceId + "'";
            DataTable dt = cn.getData(sqlStr);
            if (dt.Rows.Count == 0)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, SecurityHomeEncryption.encrypt("This device is not found"));
            }

            cn.closeHomeDeviceSocket(homeDevice);

            Task.Run(() =>
            {
                Thread.Sleep(5000);
                cn.checkHomeDeviceSocket(homeDevice);
            });
            
            return Request.CreateResponse(HttpStatusCode.OK, SecurityHomeEncryption.encrypt("You have five second to reconnect web socket!"));
        }
    }
}
