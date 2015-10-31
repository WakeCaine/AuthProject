using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AuthClient
{
    public enum Status
    {
        IDLE,
        CONNECTED,
        CONFIRM,
        REGISTERED,
        LOGIN,
        LOGGED,
        DISCONNECTED,
        BYE,
        ERROR,
        MALFORMED
    }

    public class Connection
    {
        private Status status = Status.IDLE;
        
        public void setStatus(Status status)
        {
            this.status = status;
        }

        public Status getStatus()
        {
            return status;
        }

        public Status getAnswer(String message)
        {
            if(message[0] != '|')
            {
                return Status.MALFORMED;
            }

            int count = (int)Char.GetNumericValue(message[1]);
            String cutmessage = message.Substring(2,count);
            if (cutmessage == "BYE")
            {
                return Status.BYE;
            }
            else if (cutmessage == "READY")
            {
                return Status.CONNECTED;
            }
            else if (cutmessage == "OKLOG" || cutmessage == "NOLOG")
            {
                if (cutmessage == "OKLOG")
                    return Status.LOGGED;
                else
                    return Status.ERROR;
            }
            else if (cutmessage == "KEY")
            {
                ;
                return Status.CONFIRM;
            }
            else if (cutmessage == "OKREG" || cutmessage == "NOREG")
            {
                if (cutmessage == "OKREG")
                    return Status.REGISTERED;
                else
                    return Status.ERROR;
            }
            else
                return Status.MALFORMED;
        }
    }
}
