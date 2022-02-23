using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data;
using System.Data.SqlClient;
using Security_Home_2.Models.Model;
using Newtonsoft.Json;
using System.Threading;
using System.Threading.Tasks;
using Security_Home_2.Models.Encryption;

namespace Security_Home_2.Models
{
    public class Connection
    {
        private static SqlConnectionStringBuilder cnStr;
        private static SqlConnection cn;
        private static List<String> webSocketHomeDevice = new List<String>();

        public void connect()
        {
            cnStr = new SqlConnectionStringBuilder();
            cn = new SqlConnection();

            cnStr.DataSource = @"dataproject.cwsxjrsoa7kr.us-east-1.rds.amazonaws.com";
            cnStr.InitialCatalog = "Security_Home_2";
            cnStr.UserID = "username";
            cnStr.Password = "qweasd08311";
            cnStr.ConnectTimeout = 30;
            cnStr.MaxPoolSize = 10000;

            cn.ConnectionString = cnStr.ConnectionString;
        }

        public DataTable getData(String sqlStr)
        {
            SqlDataAdapter da = new SqlDataAdapter(sqlStr, cn);
            DataTable dt = new DataTable();
            da.Fill(dt);

            return dt;
        }

        public ConnectSocket checkHomeDeviceSocket(HomeDevice encryptionReciver)
        {
            HomeDevice reciver = SecurityHomeEncryption.decrypt<HomeDevice>(encryptionReciver);

            if(webSocketHomeDevice.Where(item => item == (String)reciver.deviceId).ToList().Count != 0)
            {
                return ConnectSocket.AlreadyConnect;
            }
            else
            {
                String sqlStr = "SELECT Device_ID From Home.Device WHERE Chip_ID = '" + reciver.chipId + "' AND Mac_Address = '" + reciver.macAddress +"' AND Device_ID = '" + reciver.deviceId + "'";
                DataTable dt = getData(sqlStr);
                if (dt.Rows.Count == 0)
                {
                    return ConnectSocket.NotFindDevice;
                }
                else
                {
                    webSocketHomeDevice.Add((String)reciver.deviceId);

                    return ConnectSocket.Success;
                }
            }
        }

        public void closeHomeDeviceSocket(HomeDevice reciver)
        {
            webSocketHomeDevice.Remove((String)reciver.deviceId);
        }

        public String getConnectionString()
        {
            return cnStr.ConnectionString;
        }
    }
}