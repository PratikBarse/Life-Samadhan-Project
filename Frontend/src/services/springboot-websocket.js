import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class SpringBootWebSocketService {
    constructor() {
        this.stompClient = null;
        this.callbacks = [];
        this.isConnected = false;
    }

    startConnection(token, user) {
        if (this.isConnected && this.currentUser?.userId === user.userId) return;

        if (this.isConnected) {
            this.stopConnection();
        }

        this.currentUser = user;
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.debug = () => { };

        this.stompClient.connect({ Authorization: `Bearer ${token}` }, (frame) => {
            console.log('Connected to Spring Boot WebSocket: ' + frame);
            this.isConnected = true;


            this.stompClient.subscribe('/topic/notifications', (message) => {
                const notification = JSON.parse(message.body);
                this.callbacks.forEach(callback => callback(notification));
            });


            if (user.role === 'SERVICEPROVIDER' && user.providerId) {
                console.log(`Subscribing to provider specific topic: /topic/provider-${user.providerId}`);
                this.stompClient.subscribe(`/topic/provider-${user.providerId}`, (message) => {
                    const data = JSON.parse(message.body);
                    this.callbacks.forEach(callback => callback({ ...data, type: 'provider_update' }));
                });
            }


            if (user.role === 'CUSTOMER' && (user.customerProfileId || user.customerId)) {
                const cId = user.customerProfileId || user.customerId;
                console.log(`Subscribing to customer topic: /topic/customer-${cId}`);
                this.stompClient.subscribe(`/topic/customer-${cId}`, (message) => {
                    const data = JSON.parse(message.body);
                    this.callbacks.forEach(callback => callback({ ...data, type: 'customer_update' }));
                });
            }

            // Universal User Topic (Most reliable)
            if (user.userId) {
                console.log(`Subscribing to universal user topic: /topic/user-${user.userId}`);
                this.stompClient.subscribe(`/topic/user-${user.userId}`, (message) => {
                    const data = JSON.parse(message.body);
                    this.callbacks.forEach(callback => callback({ ...data, type: 'user_notification' }));
                });
            }
        }, (error) => {
            console.error('WebSocket connection error:', error);
            this.isConnected = false;
        });
    }

    onReceiveNotification(callback) {
        this.callbacks.push(callback);
    }

    stopConnection() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        this.isConnected = false;
    }
}

export default new SpringBootWebSocketService();
