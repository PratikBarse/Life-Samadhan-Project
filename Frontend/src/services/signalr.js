import { HubConnectionBuilder, LogLevel } from '@microsoft/signalr';

class SignalRService {
    constructor() {
        this.connection = null;
        this.callbacks = {};
    }

    startConnection(token, user) {
        if (this.connection) return;

        const HUB_URL = "/hubs/notifications";

        this.connection = new HubConnectionBuilder()
            .withUrl(HUB_URL, {
                accessTokenFactory: () => token
            })
            .withAutomaticReconnect()
            .configureLogging(LogLevel.Information)
            .build();

        this.connection.on("ReceiveNotification", (message) => {
            console.log("SignalR Notification Received:", message);
            if (this.callbacks["ReceiveNotification"]) {
                this.callbacks["ReceiveNotification"].forEach(cb => cb(message));
            }
        });

        this.connection.start()
            .then(() => console.log("SignalR Connected."))
            .catch(err => console.error("SignalR Connection Error: ", err));
    }

    stopConnection() {
        if (this.connection) {
            this.connection.stop();
            this.connection = null;
        }
    }

    onReceiveNotification(callback) {
        this.on("ReceiveNotification", callback);
    }

    on(event, callback) {
        if (!this.callbacks[event]) {
            this.callbacks[event] = [];
        }
        this.callbacks[event].push(callback);
    }

    off(event, callback) {
        if (!this.callbacks[event]) return;
        this.callbacks[event] = this.callbacks[event].filter(cb => cb !== callback);
    }
}

export default new SignalRService();
