using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace Security_Home_2.Models
{
    public enum EncryptionStatus
    {
        Encryption,
        Decryption
    }

    public enum ConnectSocket
    {
        Success,
        NotFindDevice,
        AlreadyConnect
    }
}