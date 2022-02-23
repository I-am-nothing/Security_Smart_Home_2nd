using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web;

namespace Security_Home_2.Models.Model
{
    public class Version
    {
        public Object Version_ID { set; get; }
        public Object Type_ID { set; get; }
        public Object Title { set; get; }
        public Object Detail { set; get; }
        public String BinFile { set; get; }
    }
}