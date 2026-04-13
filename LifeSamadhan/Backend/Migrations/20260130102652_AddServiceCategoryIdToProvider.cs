using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace LifeSamadhan.API.Migrations
{
    
    public partial class AddServiceCategoryIdToProvider : Migration
    {
        
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<long>(
                name: "ServiceCategoryId",
                table: "ServiceProviders",
                type: "bigint",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_ServiceProviders_ServiceCategoryId",
                table: "ServiceProviders",
                column: "ServiceCategoryId");

            migrationBuilder.AddForeignKey(
                name: "FK_ServiceProviders_ServiceCategories_ServiceCategoryId",
                table: "ServiceProviders",
                column: "ServiceCategoryId",
                principalTable: "ServiceCategories",
                principalColumn: "Id");
        }

        
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_ServiceProviders_ServiceCategories_ServiceCategoryId",
                table: "ServiceProviders");

            migrationBuilder.DropIndex(
                name: "IX_ServiceProviders_ServiceCategoryId",
                table: "ServiceProviders");

            migrationBuilder.DropColumn(
                name: "ServiceCategoryId",
                table: "ServiceProviders");
        }
    }
}
