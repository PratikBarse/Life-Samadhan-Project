using System.ComponentModel.DataAnnotations;

namespace LifeSamadhan.API.Models
{
    public class SupportTicket
    {
        [Key]
        public long Id { get; set; }

        
        
        public long UserId { get; set; }

        [Required(ErrorMessage = "Subject is required")]
        [StringLength(200, MinimumLength = 5,
            ErrorMessage = "Subject must be between 5 and 200 characters")]
        public string Subject { get; set; } = string.Empty;

        [RegularExpression("^(OPEN|IN_PROGRESS|RESOLVED|CLOSED)$",
            ErrorMessage = "Status must be OPEN, IN_PROGRESS, RESOLVED or CLOSED")]
        public string Status { get; set; } = "OPEN";
    }
}