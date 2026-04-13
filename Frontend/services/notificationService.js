import signalRService from './signalr';
import springBootService from './springboot-websocket';

class NotificationService {
    constructor() {
        this.service = null;
    }

    startConnection(token, user) {
        // Evaluate service type at the moment of connection
        // Strictly use the environment variable to avoid stale localStorage issues
        const type = import.meta.env.VITE_BACKEND_TYPE || 'SPRINGBOOT';
        this.service = (type === 'DOTNET') ? signalRService : springBootService;
        console.log(`Starting notifications for detected type: ${type}`);

        return this.service.startConnection(token, user);
    }

    onReceiveNotification(callback) {
        if (this.service) {
            this.service.onReceiveNotification(callback);
        } else {
            console.warn("NotificationService: Service not initialized yet.");
        }
    }

    stopConnection() {
        if (signalRService.stopConnection) signalRService.stopConnection();
        if (springBootService.stopConnection) springBootService.stopConnection();
    }
}

export default new NotificationService();
