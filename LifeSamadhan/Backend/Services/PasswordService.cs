using System;
using System.Security.Cryptography;
using System.Text;

namespace LifeSamadhan.API.Services
{
    public class PasswordService
    {
        public string Hash(string p)
        {
            using (var sha = SHA256.Create())
            {
                return Convert.ToBase64String(
                    sha.ComputeHash(Encoding.UTF8.GetBytes(p))
                );
            }
        }

        public bool Verify(string p, string h)
        {
            return Hash(p) == h;
        }
    }
}
