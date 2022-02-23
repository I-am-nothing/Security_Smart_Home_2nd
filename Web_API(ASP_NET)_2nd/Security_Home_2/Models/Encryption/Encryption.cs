using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace Security_Home_2.Models.Encryption
{
    public class Encryption
    {
        public String key;
        public String value;

        public Encryption(String key, String value)
        {
            this.key = key;
            this.value = value;
        }
    }
}