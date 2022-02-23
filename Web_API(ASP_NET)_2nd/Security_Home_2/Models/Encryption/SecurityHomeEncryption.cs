using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Web;
using System.Text;
using System.Data;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Diagnostics;

namespace Security_Home_2.Models.Encryption
{
    public static class SecurityHomeEncryption
    {
        public static T encrypt<T>(object model)
        {
            return (T)modelRun(model, EncryptionStatus.Encryption);
        }

        public static JArray encrypt(DataTable data)
        {
            return dataTableRun(data, EncryptionStatus.Encryption);
        }

        public static Encryption encrypt(String value)
        {
            String uuid = Guid.NewGuid().ToString();
            Byte[] encryptionText = value.ToCharArray().Select(c => (Byte)c).ToArray();
            Byte[] uuidByte = uuid.ToCharArray().Select(c => (Byte)c).ToArray();

            for(int i=0, j=0; i < encryptionText.Length; i++, j++)
            {
                if (j == uuidByte.Length)
                {
                    j = 0;
                }

                encryptionText[i] ^= uuidByte[j];
            }

            for (int i = 0, j = uuidByte.Length-1; i < encryptionText.Length; i++, j--)
            {
                if (j == -1)
                {
                    j = uuidByte.Length -1;
                }

                encryptionText[i] ^= uuidByte[j];
            }

            return new Encryption(uuid, Encoding.Default.GetString(encryptionText));
        }

        public static T decrypt<T>(object model)
        {
            return (T)modelRun(model, EncryptionStatus.Decryption);
        }

        public static JArray decrypt(DataTable data)
        {
            return dataTableRun(data, EncryptionStatus.Decryption);
        }

        public static String decrypt(Encryption encryption)
        {
            Byte[] uuidByte = encryption.key.ToCharArray().Select(c => (Byte)c).ToArray();
            Byte[] decryptionText = encryption.value.ToCharArray().Select(c => (Byte)c).ToArray();

            for (int i = 0, j = uuidByte.Length - 1; i < decryptionText.Length; i++, j--)
            {
                if (j == -1)
                {
                    j = uuidByte.Length - 1;
                }

                decryptionText[i] ^= uuidByte[j];
            }

            for (int i = 0, j = 0; i < decryptionText.Length; i++, j++)
            {
                if (j == uuidByte.Length)
                {
                    j = 0;
                }

                decryptionText[i] ^= uuidByte[j];
            }

            return Encoding.Default.GetString(decryptionText);
        }

        private static JArray dataTableRun(DataTable data, EncryptionStatus status)
        {
            JArray json = JArray.Parse(JsonConvert.SerializeObject(data));
            IEnumerable<String> columnNames = data.Columns.Cast<DataColumn>().Select(column => column.ColumnName);

            for (int i=0; i<data.Rows.Count; i++)
            {
                foreach(String columnName in columnNames)
                {
                    if(columnName != "Bin_File")
                    {
                        if(status == EncryptionStatus.Encryption)
                        {
                            json[i][columnName] = JObject.Parse(JsonConvert.SerializeObject(encrypt(json[i][columnName].ToString())));
                        }
                        else if(status == EncryptionStatus.Decryption)
                        {
                            json[i][columnName] = decrypt(JsonConvert.DeserializeObject<Encryption>(json[i][columnName].ToString()));
                        }
                    }
                }
            }

            return json;
        }

        private static object modelRun(object model, EncryptionStatus status)
        {
            FieldInfo[] fields = model.GetType().GetFields().ToArray();

            PropertyInfo[] properties = model.GetType().GetProperties().ToArray();

            foreach (FieldInfo i in fields)
            {
                if(i.GetValue(model) != null)
                {
                    if (status == EncryptionStatus.Encryption)
                    {
                        String value = i.GetValue(model).ToString();
                        i.SetValue(model, encrypt(value));
                    }
                    else if(status == EncryptionStatus.Decryption)
                    {
                        Encryption value = (Encryption)i.GetValue(model);
                        i.SetValue(model, decrypt(value));
                    }
                }
            }

            foreach (PropertyInfo i in properties)
            {
                if (i.GetValue(model) != null)
                {
                    if (status == EncryptionStatus.Encryption)
                    {
                        String value = i.GetValue(model).ToString();
                        i.SetValue(model, encrypt(value));
                    }
                    else if (status == EncryptionStatus.Decryption)
                    {
                        Encryption value = JsonConvert.DeserializeObject<Encryption>(JsonConvert.SerializeObject(i.GetValue(model)));

                        i.SetValue(model, decrypt(value));
                    }
                }
            }

            return model;
        }
    }
}