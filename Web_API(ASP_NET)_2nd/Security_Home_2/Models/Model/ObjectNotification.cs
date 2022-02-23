using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace Security_Home_2.Models.Model
{
    public class ObjectNotification
    {
        public Object Command_ID;

        public ObjectNotification(Notification notification)
        {
            Command_ID = notification.Command_ID;
        }
    }
}