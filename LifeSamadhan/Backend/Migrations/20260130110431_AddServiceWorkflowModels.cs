using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace LifeSamadhan.API.Migrations
{
    
    public partial class AddServiceWorkflowModels : Migration
    {
        
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Ratings_ServiceAssignments_AssignmentId",
                table: "Ratings");

            migrationBuilder.DropColumn(
                name: "Feedback",
                table: "Ratings");

            migrationBuilder.RenameColumn(
                name: "RatedAt",
                table: "Ratings",
                newName: "CreatedAt");

            migrationBuilder.RenameColumn(
                name: "ProviderId",
                table: "Ratings",
                newName: "ServiceRequestId");

            migrationBuilder.RenameColumn(
                name: "CustomerId",
                table: "Ratings",
                newName: "ReviewerId");

            migrationBuilder.RenameColumn(
                name: "AssignmentId",
                table: "Ratings",
                newName: "RevieweeId");

            migrationBuilder.RenameIndex(
                name: "IX_Ratings_AssignmentId",
                table: "Ratings",
                newName: "IX_Ratings_RevieweeId");

            migrationBuilder.AddColumn<decimal>(
                name: "Amount",
                table: "ServiceRequests",
                type: "decimal(65,30)",
                nullable: false,
                defaultValue: 0m);

            migrationBuilder.AddColumn<DateTime>(
                name: "CompletionDate",
                table: "ServiceRequests",
                type: "datetime(6)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "OTP",
                table: "ServiceRequests",
                type: "longtext",
                nullable: true)
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.AddColumn<string>(
                name: "PaymentStatus",
                table: "ServiceRequests",
                type: "longtext",
                nullable: false)
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.AddColumn<long>(
                name: "ProviderId",
                table: "ServiceRequests",
                type: "bigint",
                nullable: true);

            migrationBuilder.AddColumn<DateTime>(
                name: "ScheduledDate",
                table: "ServiceRequests",
                type: "datetime(6)",
                nullable: false,
                defaultValue: new DateTime(1, 1, 1, 0, 0, 0, 0, DateTimeKind.Unspecified));

            migrationBuilder.AddColumn<string>(
                name: "ServiceAddress",
                table: "ServiceRequests",
                type: "longtext",
                nullable: false)
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.AddColumn<string>(
                name: "Comment",
                table: "Ratings",
                type: "longtext",
                nullable: false)
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.AddColumn<string>(
                name: "Address",
                table: "CustomerProfiles",
                type: "varchar(500)",
                maxLength: 500,
                nullable: false,
                defaultValue: "")
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateIndex(
                name: "IX_ServiceRequests_CustomerId",
                table: "ServiceRequests",
                column: "CustomerId");

            migrationBuilder.CreateIndex(
                name: "IX_ServiceRequests_LocationId",
                table: "ServiceRequests",
                column: "LocationId");

            migrationBuilder.CreateIndex(
                name: "IX_ServiceRequests_ProviderId",
                table: "ServiceRequests",
                column: "ProviderId");

            migrationBuilder.CreateIndex(
                name: "IX_ServiceRequests_ServiceId",
                table: "ServiceRequests",
                column: "ServiceId");

            migrationBuilder.CreateIndex(
                name: "IX_Ratings_ReviewerId",
                table: "Ratings",
                column: "ReviewerId");

            migrationBuilder.CreateIndex(
                name: "IX_Ratings_ServiceRequestId",
                table: "Ratings",
                column: "ServiceRequestId");

            migrationBuilder.AddForeignKey(
                name: "FK_Ratings_ServiceRequests_ServiceRequestId",
                table: "Ratings",
                column: "ServiceRequestId",
                principalTable: "ServiceRequests",
                principalColumn: "Id",
                onDelete: ReferentialAction.Restrict);

            migrationBuilder.AddForeignKey(
                name: "FK_Ratings_Users_RevieweeId",
                table: "Ratings",
                column: "RevieweeId",
                principalTable: "Users",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_Ratings_Users_ReviewerId",
                table: "Ratings",
                column: "ReviewerId",
                principalTable: "Users",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_ServiceRequests_Locations_LocationId",
                table: "ServiceRequests",
                column: "LocationId",
                principalTable: "Locations",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_ServiceRequests_ServiceProviders_ProviderId",
                table: "ServiceRequests",
                column: "ProviderId",
                principalTable: "ServiceProviders",
                principalColumn: "Id");

            migrationBuilder.AddForeignKey(
                name: "FK_ServiceRequests_Services_ServiceId",
                table: "ServiceRequests",
                column: "ServiceId",
                principalTable: "Services",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "FK_ServiceRequests_Users_CustomerId",
                table: "ServiceRequests",
                column: "CustomerId",
                principalTable: "Users",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }

        
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Ratings_ServiceRequests_ServiceRequestId",
                table: "Ratings");

            migrationBuilder.DropForeignKey(
                name: "FK_Ratings_Users_RevieweeId",
                table: "Ratings");

            migrationBuilder.DropForeignKey(
                name: "FK_Ratings_Users_ReviewerId",
                table: "Ratings");

            migrationBuilder.DropForeignKey(
                name: "FK_ServiceRequests_Locations_LocationId",
                table: "ServiceRequests");

            migrationBuilder.DropForeignKey(
                name: "FK_ServiceRequests_ServiceProviders_ProviderId",
                table: "ServiceRequests");

            migrationBuilder.DropForeignKey(
                name: "FK_ServiceRequests_Services_ServiceId",
                table: "ServiceRequests");

            migrationBuilder.DropForeignKey(
                name: "FK_ServiceRequests_Users_CustomerId",
                table: "ServiceRequests");

            migrationBuilder.DropIndex(
                name: "IX_ServiceRequests_CustomerId",
                table: "ServiceRequests");

            migrationBuilder.DropIndex(
                name: "IX_ServiceRequests_LocationId",
                table: "ServiceRequests");

            migrationBuilder.DropIndex(
                name: "IX_ServiceRequests_ProviderId",
                table: "ServiceRequests");

            migrationBuilder.DropIndex(
                name: "IX_ServiceRequests_ServiceId",
                table: "ServiceRequests");

            migrationBuilder.DropIndex(
                name: "IX_Ratings_ReviewerId",
                table: "Ratings");

            migrationBuilder.DropIndex(
                name: "IX_Ratings_ServiceRequestId",
                table: "Ratings");

            migrationBuilder.DropColumn(
                name: "Amount",
                table: "ServiceRequests");

            migrationBuilder.DropColumn(
                name: "CompletionDate",
                table: "ServiceRequests");

            migrationBuilder.DropColumn(
                name: "OTP",
                table: "ServiceRequests");

            migrationBuilder.DropColumn(
                name: "PaymentStatus",
                table: "ServiceRequests");

            migrationBuilder.DropColumn(
                name: "ProviderId",
                table: "ServiceRequests");

            migrationBuilder.DropColumn(
                name: "ScheduledDate",
                table: "ServiceRequests");

            migrationBuilder.DropColumn(
                name: "ServiceAddress",
                table: "ServiceRequests");

            migrationBuilder.DropColumn(
                name: "Comment",
                table: "Ratings");

            migrationBuilder.DropColumn(
                name: "Address",
                table: "CustomerProfiles");

            migrationBuilder.RenameColumn(
                name: "ServiceRequestId",
                table: "Ratings",
                newName: "ProviderId");

            migrationBuilder.RenameColumn(
                name: "ReviewerId",
                table: "Ratings",
                newName: "CustomerId");

            migrationBuilder.RenameColumn(
                name: "RevieweeId",
                table: "Ratings",
                newName: "AssignmentId");

            migrationBuilder.RenameColumn(
                name: "CreatedAt",
                table: "Ratings",
                newName: "RatedAt");

            migrationBuilder.RenameIndex(
                name: "IX_Ratings_RevieweeId",
                table: "Ratings",
                newName: "IX_Ratings_AssignmentId");

            migrationBuilder.AddColumn<string>(
                name: "Feedback",
                table: "Ratings",
                type: "longtext",
                nullable: true)
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.AddForeignKey(
                name: "FK_Ratings_ServiceAssignments_AssignmentId",
                table: "Ratings",
                column: "AssignmentId",
                principalTable: "ServiceAssignments",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
