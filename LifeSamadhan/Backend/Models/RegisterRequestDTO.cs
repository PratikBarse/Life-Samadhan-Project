using System.ComponentModel.DataAnnotations;

namespace LifeSamadhan.API.Models
{
    public class RegisterRequestDTO
    {
        
        [Required(ErrorMessage = "Name is required")]
        [StringLength(100, MinimumLength = 3,
            ErrorMessage = "Name must be between 3 and 100 characters")]
        public string Name { get; set; } = string.Empty;

        [Required(ErrorMessage = "Email is required")]
        [EmailAddress(ErrorMessage = "Invalid email format")]
        public string Email { get; set; } = string.Empty;

        [Required(ErrorMessage = "Mobile number is required")]
        [RegularExpression(@"^[6-9]\d{9}$",
            ErrorMessage = "Invalid Indian mobile number")]
        public string Mobile { get; set; } = string.Empty;

        [Required(ErrorMessage = "Password is required")]
        [RegularExpression(
            @"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$",
            ErrorMessage = "Password must contain at least 1 uppercase, 1 lowercase, 1 number, 1 special character and minimum 8 characters"
        )]
        public string Password { get; set; } = string.Empty;

        [Required(ErrorMessage = "Role is required")]
        [RegularExpression("^(CUSTOMER|SERVICEPROVIDER|ADMIN)$",
            ErrorMessage = "Role must be CUSTOMER, SERVICEPROVIDER or ADMIN")]
        public string Role { get; set; } = string.Empty;

        
        public string? ProviderType { get; set; }
        
        [Range(0, 100000, ErrorMessage = "Hourly rate must be between 0 and 100000")]
        public decimal? HourlyRate { get; set; }
        
        public string? City { get; set; }
        
        public string? State { get; set; }

        
        public string? Skills { get; set; }

        public long? ServiceCategoryId { get; set; }
        
        public string? Address { get; set; }

        public long? LocationId { get; set; }
    }
}
