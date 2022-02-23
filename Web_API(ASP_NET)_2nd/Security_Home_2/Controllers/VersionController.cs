using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Threading;
using System.Threading.Tasks;
using System.Data;
using System.Data.SqlClient;
using Security_Home_2.Models;
using Security_Home_2.Models.Encryption;
using Security_Home_2.Models.Model;
using Newtonsoft.Json;
using Version = Security_Home_2.Models.Model.Version;
using Security_Home_2.Models.Response;
using static System.Net.WebRequestMethods;
using System.IO;
using System.Net.Http.Headers;
using System.Text;
using System.Diagnostics;

namespace Security_Home_2.Controllers
{
    public class VersionController : ApiController
    {
        private Connection cn = new Connection();

        [Route("version/home_device")]
        [HttpGet]
        public HttpResponseMessage getHomeDeviceVersion()
        {
            String sqlStr = "SELECT Version_ID, Type_ID, Title, Detail FROM Version.Home_Device";
            DataTable dt = cn.getData(sqlStr);

            return Request.CreateResponse(HttpStatusCode.OK, new Success("1", SecurityHomeEncryption.encrypt(dt)));
        }

        [Route("version/home_device")]
        [HttpPost]
        public HttpResponseMessage homeDeviceVersion([FromBody] Version encryptionVersion)
        {
            Version version = SecurityHomeEncryption.decrypt<Version>(encryptionVersion);
            String sqlStr = "SELECT TOP(1) Version_ID, Type_ID, Title, Detail FROM Version.Home_Device WHERE Version_ID > '" + version.Version_ID + "' AND Type_ID = '" + version.Type_ID + "' ORDER BY Version_ID DESC";
            DataTable dt = cn.getData(sqlStr);

            if (dt.Rows.Count == 0)
            {
                String sqlStr2 = "SELECT TOP(1) Type_ID FROM Version.Home_Device WHERE Type_ID = '" + version.Type_ID + "'";
                if (cn.getData(sqlStr2).Rows.Count == 0)
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, SecurityHomeEncryption.encrypt("Can not find this type version."));
                }
                
                return Request.CreateResponse(HttpStatusCode.OK, new Success("1", "Success"));
            }
            
            if ((int)dt.Rows[0].ItemArray[0].ToString().Split('.')[1][0] == (int)((String)(version.Version_ID)).Split('.')[1][0]) 
            {
                return Request.CreateResponse(HttpStatusCode.OK, new Success("2", SecurityHomeEncryption.encrypt(dt)[0]));
            }
                
            return Request.CreateResponse(HttpStatusCode.OK, new Success("0", SecurityHomeEncryption.encrypt(dt)[0]));
        }

        [Route("version/home_device/update")]
        [HttpPost]
        public HttpResponseMessage homeDeviceUpdate([FromBody] Version encryptionVersion)
        {
            Version version = SecurityHomeEncryption.decrypt<Version>(encryptionVersion);
            String sqlStr = "SELECT TOP(1) Bin_File FROM Version.Home_Device where Version_ID > '" + version.Version_ID + "' AND Type_ID = '" + version.Type_ID + "' ORDER BY Version_ID DESC";
            DataTable dt = cn.getData(sqlStr);

            if (dt.Rows.Count == 0)
            {
                String sqlStr2 = "SELECT TOP(1) Type_ID FROM Version.Home_Device WHERE Type_ID = '" + version.Type_ID + "'";
                if (cn.getData(sqlStr2).Rows.Count == 0)
                {
                    return Request.CreateResponse(HttpStatusCode.BadRequest, SecurityHomeEncryption.encrypt("Can not find this type version."));
                }
                
                return Request.CreateResponse(HttpStatusCode.BadRequest, SecurityHomeEncryption.encrypt("Your version is already the latest version!"));
            }

            String file = Convert.ToBase64String((byte[])cn.getData(sqlStr).Rows[0].ItemArray[0]);
            Stream ms = new MemoryStream(Convert.FromBase64String(file));
            HttpResponseMessage response = new HttpResponseMessage(HttpStatusCode.OK);
            response.Content = new StreamContent(ms);
            response.Content.Headers.ContentType = new MediaTypeHeaderValue("application/octet-stream");

            return response;
        }

        [Route("version/android")]
        [HttpPost]
        public HttpResponseMessage androidVersion([FromBody] Version encryptionVersion)
        {
            Version version = SecurityHomeEncryption.decrypt<Version>(encryptionVersion);
            String sqlStr = "SELECT TOP(1) Version_ID, Title, Detail FROM Version.Android Where Version_ID > '" + version.Version_ID + "' ORDER BY Version_ID DESC";
            DataTable dt = cn.getData(sqlStr);

            if(dt.Rows.Count == 0)
            {
                return Request.CreateResponse(HttpStatusCode.OK, new Success("1", "Success"));
            }
            
            if((int)dt.Rows[0].ItemArray[0].ToString().Split('.')[1][0] == (int)((String)version.Version_ID).Split('.')[1][0])
            {
                return Request.CreateResponse(HttpStatusCode.OK, new Success("2", SecurityHomeEncryption.encrypt(dt)[0]));
            }
            
            return Request.CreateResponse(HttpStatusCode.OK, new Success("0", SecurityHomeEncryption.encrypt(dt)[0]));
        }

        [Route("version/update")]
        [HttpPost]
        public HttpResponseMessage transferBin([FromBody] Version encryptionVersion)
        {
            Byte[] binFile = Convert.FromBase64String(encryptionVersion.BinFile);
            encryptionVersion.BinFile = null;
            Version version = SecurityHomeEncryption.decrypt<Version>(encryptionVersion);
            Trace.WriteLine(JsonConvert.SerializeObject(version));
            
            String sqlStr = "SELECT TOP(1) Version_ID FROM Version.Home_Device WHERE Version_ID >= '" + version.Version_ID + "' AND Type_ID = '" + version.Type_ID + "' ORDER BY Version_ID DESC";

            if (cn.getData(sqlStr).Rows.Count == 1)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, SecurityHomeEncryption.encrypt("Can not find this type version."));
            }
            
            String sqlStr2 = "SET QUOTED_IDENTIFIER OFF SET ANSI_NULLS ON INSERT INTO Version.Home_Device VALUES('" + version.Version_ID + "', '" + version.Type_ID + "', '" + version.Title + "', '" + version.Detail + "', 0x" + BitConverter.ToString(binFile).Replace("-", "") + " )";
            cn.getData(sqlStr2);

            return Request.CreateResponse(HttpStatusCode.OK, new Success("1", "Upload Success"));
        }
    }
}
