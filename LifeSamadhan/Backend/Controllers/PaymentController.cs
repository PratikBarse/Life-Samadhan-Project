using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using LifeSamadhan.API.Data;
using LifeSamadhan.API.Models;
using Microsoft.Extensions.Configuration;
using System;
using System.Linq;
using System.Collections.Generic;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;

namespace LifeSamadhan.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/payment")]
    public class PaymentController : ControllerBase
    {
        private readonly LifeSamadhanDbContext _db;
        private readonly string _keyId;
        private readonly string _keySecret;

        public PaymentController(LifeSamadhanDbContext db, IConfiguration config)
        {
            _db = db;
            _keyId = config["Razorpay:KeyId"] ?? "";
            _keySecret = config["Razorpay:KeySecret"] ?? "";
        }

        [HttpPost("create-order/{assignmentId}")]
        public IActionResult CreateOrder(long assignmentId)
        {
            var assignment = _db.ServiceAssignments.Find(assignmentId);
            if (assignment == null) return NotFound("Assignment not found");

            var request = _db.ServiceRequests.Find(assignment.RequestId);
            if (request == null) return NotFound("Request not found");

            if (request.Amount <= 0) return BadRequest("Invalid amount for payment");

            Razorpay.Api.RazorpayClient client = new Razorpay.Api.RazorpayClient(_keyId, _keySecret);

            Dictionary<string, object> options = new Dictionary<string, object>();
            options.Add("amount", (int)(request.Amount * 100)); 
            options.Add("currency", "INR");
            options.Add("receipt", "rcpt_" + assignmentId);
            
            try
            {
                Razorpay.Api.Order order = client.Order.Create(options);
                string orderId = order["id"].ToString();

                
                var paymentRecord = new LifeSamadhan.API.Models.Payment
                {
                    AssignmentId = assignmentId,
                    CustomerId = request.CustomerId,
                    ProviderId = assignment.ProviderId,
                    Amount = (double)request.Amount,
                    PaymentStatus = "PENDING",
                    PaymentStatusExtra = "PENDING", 
                    RazorpayOrderId = orderId,
                    CreatedAt = DateTime.Now
                };

                _db.Payments.Add(paymentRecord);
                _db.SaveChanges();

                
                if (request != null)
                {
                    request.PaymentStatus = "PENDING"; 
                }
                _db.SaveChanges(); 

                return Ok(new
                {
                    orderId = orderId,
                    amount = request.Amount,
                    currency = "INR",
                    keyId = _keyId
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ERROR] Payment Creation Failed: {ex.Message}");
                if (ex.InnerException != null)
                {
                     Console.WriteLine($"[ERROR] Inner Exception: {ex.InnerException.Message}");
                     return StatusCode(500, new { error = "Failed to create Razorpay order: " + ex.InnerException.Message });
                }
                return StatusCode(500, new { error = "Failed to create Razorpay order: " + ex.Message });
            }
        }

        [HttpPost("verify")]
        public IActionResult VerifyPayment([FromBody] PaymentVerificationReq dto)
        {
            var paymentRecord = _db.Payments.FirstOrDefault(p => p.RazorpayOrderId == dto.RazorpayOrderId);
            if (paymentRecord == null) return NotFound("Payment record not found");

            
            string generatedSignature = PaymentUtils.GetGeneratedSignature(dto.RazorpayOrderId, dto.RazorpayPaymentId, _keySecret);

            if (generatedSignature == dto.RazorpaySignature)
            {
                paymentRecord.PaymentStatus = "SUCCESS";
                paymentRecord.RazorpayPaymentId = dto.RazorpayPaymentId;
                paymentRecord.RazorpaySignature = dto.RazorpaySignature;
                paymentRecord.PaidAt = DateTime.Now;

                
                var assignment = _db.ServiceAssignments.Find(paymentRecord.AssignmentId);
                if (assignment != null)
                {
                    var req = _db.ServiceRequests.Find(assignment.RequestId);
                    if (req != null)
                    {
                        req.PaymentStatus = "PAID";
                        req.Status = "PAID"; 
                    }
                }

                _db.SaveChanges();
                return Ok(new { message = "Payment verified successfully", paymentId = paymentRecord.Id });
            }
            else
            {
                paymentRecord.PaymentStatus = "FAILED";
                _db.SaveChanges();
                return BadRequest("Invalid payment signature");
            }
        }
    }

    public class PaymentVerificationReq
    {
        public string RazorpayOrderId { get; set; } = string.Empty;
        public string RazorpayPaymentId { get; set; } = string.Empty;
        public string RazorpaySignature { get; set; } = string.Empty;
    }

    public static class PaymentUtils
    {
        public static string GetGeneratedSignature(string orderId, string paymentId, string secret)
        {
            string payload = orderId + "|" + paymentId;
            using (var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(secret)))
            {
                byte[] hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(payload));
                return BitConverter.ToString(hash).Replace("-", "").ToLower();
            }
        }
    }
}
