using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace LifeSamadhan.API.Migrations
{
    
    public partial class AllowCategoryBasedBooking : Migration
    {
        
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_ServiceRequests_Services_ServiceId",
                table: "ServiceRequests");

            migrationBuilder.AlterColumn<long>(
                name: "ServiceId",
                table: "ServiceRequests",
                type: "bigint",
                nullable: true,
                oldClrType: typeof(long),
                oldType: "bigint");

            migrationBuilder.AddColumn<long>(
                name: "ServiceCategoryId",
                table: "ServiceRequests",
                type: "bigint",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_ServiceRequests_ServiceCategoryId",
                table: "ServiceRequests",
                column: "ServiceCategoryId");

            migrationBuilder.AddForeignKey(
                name: "FK_ServiceRequests_ServiceCategories_ServiceCategoryId",
                table: "ServiceRequests",
                column: "ServiceCategoryId",
                principalTable: "ServiceCategories",
                principalColumn: "Id");

            migrationBuilder.AddForeignKey(
                name: "FK_ServiceRequests_Services_ServiceId",
                table: "ServiceRequests",
                column: "ServiceId",
                principalTable: "Services",
                principalColumn: "Id");
        }

        
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_ServiceRequests_ServiceCategories_ServiceCategoryId",
                table: "ServiceRequests");

            migrationBuilder.DropForeignKey(
                name: "FK_ServiceRequests_Services_ServiceId",
                table: "ServiceRequests");

            migrationBuilder.DropIndex(
                name: "IX_ServiceRequests_ServiceCategoryId",
                table: "ServiceRequests");

            migrationBuilder.DropColumn(
                name: "ServiceCategoryId",
                table: "ServiceRequests");

            migrationBuilder.AlterColumn<long>(
                name: "ServiceId",
                table: "ServiceRequests",
                type: "bigint",
                nullable: false,
                defaultValue: 0L,
                oldClrType: typeof(long),
                oldType: "bigint",
                oldNullable: true);

            migrationBuilder.AddForeignKey(
                name: "FK_ServiceRequests_Services_ServiceId",
                table: "ServiceRequests",
                column: "ServiceId",
                principalTable: "Services",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
