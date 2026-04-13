using Microsoft.EntityFrameworkCore;
using LifeSamadhan.API.Models;


using ServiceProviderModel = LifeSamadhan.API.Models.ServiceProvider;

namespace LifeSamadhan.API.Data
{
    public class LifeSamadhanDbContext : DbContext
    {
        public LifeSamadhanDbContext(DbContextOptions<LifeSamadhanDbContext> options)
            : base(options)
        {
        }

        public DbSet<User> Users { get; set; }
        public DbSet<CustomerProfile> CustomerProfiles { get; set; }

        public DbSet<ServiceCategory> ServiceCategories { get; set; }
        public DbSet<Service> Services { get; set; }

        public DbSet<ServiceProviderModel> ServiceProviders { get; set; }
        public DbSet<ProviderSkill> ProviderSkills { get; set; }

        public DbSet<ServiceRequest> ServiceRequests { get; set; }
        public DbSet<ServiceAssignment> ServiceAssignments { get; set; }

        public DbSet<ServicePrice> ServicePrices { get; set; }

        public DbSet<Rating> Ratings { get; set; }
        public DbSet<Payment> Payments { get; set; }

        public DbSet<Location> Locations { get; set; }
        public DbSet<Notification> Notifications { get; set; }

        public DbSet<SupportTicket> SupportTickets { get; set; }
        public DbSet<ProviderLocation> ProviderLocations { get; set; }
        public DbSet<ProviderType> ProviderTypes { get; set; }



        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            
            
            
            
            
            
            
            
            modelBuilder.Entity<Rating>()
                .HasOne(r => r.ServiceRequest)
                .WithMany()
                .HasForeignKey(r => r.ServiceRequestId)
                .OnDelete(DeleteBehavior.Restrict); 


            
            
        }
    }
}
