using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Web;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Security_Home_2.Models;
using Security_Home_2.Models.Encryption;

namespace Security_Home_2.Models.Response
{
    public class Success
    {
        public object status;
        public object message;

        public Success(String status, String message)
        {
            this.status = SecurityHomeEncryption.encrypt(status);
            this.message = SecurityHomeEncryption.encrypt(message);
        }

        public Success(String status, object message)
        {
            this.status = SecurityHomeEncryption.encrypt(status);
            this.message = message;
        }
    }
}