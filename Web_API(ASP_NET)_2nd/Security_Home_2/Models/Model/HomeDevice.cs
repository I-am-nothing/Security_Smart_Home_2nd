using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using Security_Home_2.Models.Encryption;

namespace Security_Home_2.Models.Model
{
    public class HomeDevice
    {
        public Object deviceId { set; get; }
        public Object typeId { set; get; }
        public Object chipId { set; get; }
        public Object macAddress { set; get; }
    }
}