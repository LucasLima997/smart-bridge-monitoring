package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeHealthServiceGrpc;
import distsys.smartbridge.grpc.BridgeMaintenanceServiceGrpc;
import distsys.smartbridge.grpc.BridgeSensorServiceGrpc;
import distsys.smartbridge.grpc.CreateWorkOrderReq;
import distsys.smartbridge.grpc.CreateWorkOrderRes;
import distsys.smartbridge.grpc.DiscoverServiceReq;
import distsys.smartbridge.grpc.DiscoverServiceRes;
import distsys.smartbridge.grpc.GetHealthStatusReq;
import distsys.smartbridge.grpc.GetHealthStatusRes;
import distsys.smartbridge.grpc.ListServicesReq;
import distsys.smartbridge.grpc.ListServicesRes;
import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.Priority;
import distsys.smartbridge.grpc.SensorType;
import distsys.smartbridge.grpc.ServiceInfo;
import distsys.smartbridge.grpc.SubscribeAlertsReq;
import distsys.smartbridge.grpc.SubscribeAlertsRes;
import distsys.smartbridge.grpc.ValidateReadingReq;
import distsys.smartbridge.grpc.ValidateReadingRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.swing.*;
import java.awt.*;

public class SmartBridgeGUI extends JFrame {

    private JTextArea outputArea;

    public SmartBridgeGUI() {
        setTitle("GUI");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Smart Bridge Monitoring System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton listServicesButton = new JButton("List Services");
        JButton validateSensorButton = new JButton("Validate Sensor Reading");
        JButton healthStatusButton = new JButton("Get Health Status");
        JButton createWorkOrderButton = new JButton("Create Work Order");
        JButton subscribeAlertsButton = new JButton("Subscribe Alerts");

        listServicesButton.addActionListener(e -> listServices());
        validateSensorButton.addActionListener(e -> validateSensorReading());
        healthStatusButton.addActionListener(e -> getHealthStatus());
        createWorkOrderButton.addActionListener(e -> createWorkOrder());
        subscribeAlertsButton.addActionListener(e -> subscribeAlerts());

        buttonPanel.add(listServicesButton);
        buttonPanel.add(validateSensorButton);
        buttonPanel.add(healthStatusButton);
        buttonPanel.add(createWorkOrderButton);
        buttonPanel.add(subscribeAlertsButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void listServices() {
        outputArea.setText("");

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        try {
            NamingServiceGrpc.NamingServiceBlockingStub blockingStub =
                    NamingServiceGrpc.newBlockingStub(channel);

            ListServicesRes response = blockingStub.listServices(
                    ListServicesReq.newBuilder().build()
            );

            outputArea.append("Registered Services:\n");
            outputArea.append("====================================\n");

            for (ServiceInfo service : response.getServicesList()) {
                outputArea.append("Service Name: " + service.getServiceName() + "\n");
                outputArea.append("Host: " + service.getHost() + "\n");
                outputArea.append("Port: " + service.getPort() + "\n");
                outputArea.append("Description: " + service.getDescription() + "\n");
                outputArea.append("------------------------------------\n");
            }

        } catch (Exception ex) {
            outputArea.append("Error listing services: " + ex.getMessage() + "\n");
        } finally {
            channel.shutdown();
        }
    }

    private void validateSensorReading() {
        outputArea.setText("");

        ManagedChannel namingChannel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        try {
            NamingServiceGrpc.NamingServiceBlockingStub namingStub =
                    NamingServiceGrpc.newBlockingStub(namingChannel);

            DiscoverServiceRes serviceInfo = namingStub.discoverService(
                    DiscoverServiceReq.newBuilder()
                            .setServiceName("BridgeSensorService")
                            .build()
            );

            if (!serviceInfo.getFound()) {
                outputArea.append("BridgeSensorService not found in Naming Service.\n");
                return;
            }

            ManagedChannel serviceChannel = ManagedChannelBuilder
                    .forAddress(serviceInfo.getHost(), serviceInfo.getPort())
                    .usePlaintext()
                    .build();

            try {
                BridgeSensorServiceGrpc.BridgeSensorServiceBlockingStub sensorStub =
                        BridgeSensorServiceGrpc.newBlockingStub(serviceChannel);

                ValidateReadingReq request = ValidateReadingReq.newBuilder()
                        .setBridgeId("BR-001")
                        .setSensorId("TEMP-01")
                        .setSensorType(SensorType.TEMPERATURE)
                        .setValue(22.5)
                        .setUnit("C")
                        .build();

                ValidateReadingRes response = sensorStub.validateReading(request);

                outputArea.append("Sensor Validation Result:\n");
                outputArea.append("====================================\n");
                outputArea.append("Service Host: " + serviceInfo.getHost() + "\n");
                outputArea.append("Service Port: " + serviceInfo.getPort() + "\n");
                outputArea.append("Is Valid: " + response.getIsValid() + "\n");
                outputArea.append("Message: " + response.getMessage() + "\n");

            } finally {
                serviceChannel.shutdown();
            }

        } catch (Exception ex) {
            outputArea.append("Error validating sensor reading: " + ex.getMessage() + "\n");
        } finally {
            namingChannel.shutdown();
        }
    }

    private void getHealthStatus() {
        outputArea.setText("");

        ManagedChannel namingChannel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        try {
            NamingServiceGrpc.NamingServiceBlockingStub namingStub =
                    NamingServiceGrpc.newBlockingStub(namingChannel);

            DiscoverServiceRes serviceInfo = namingStub.discoverService(
                    DiscoverServiceReq.newBuilder()
                            .setServiceName("BridgeHealthService")
                            .build()
            );

            if (!serviceInfo.getFound()) {
                outputArea.append("BridgeHealthService not found in Naming Service.\n");
                return;
            }

            ManagedChannel serviceChannel = ManagedChannelBuilder
                    .forAddress(serviceInfo.getHost(), serviceInfo.getPort())
                    .usePlaintext()
                    .build();

            try {
                BridgeHealthServiceGrpc.BridgeHealthServiceBlockingStub healthStub =
                        BridgeHealthServiceGrpc.newBlockingStub(serviceChannel);

                GetHealthStatusReq request = GetHealthStatusReq.newBuilder()
                        .setBridgeId("BR-001")
                        .build();

                GetHealthStatusRes response = healthStub.getHealthStatus(request);

                outputArea.append("Bridge Health Status:\n");
                outputArea.append("====================================\n");
                outputArea.append("Service Host: " + serviceInfo.getHost() + "\n");
                outputArea.append("Service Port: " + serviceInfo.getPort() + "\n");
                outputArea.append("Bridge ID: " + response.getBridgeId() + "\n");
                outputArea.append("Health Score: " + response.getHealthScore() + "\n");
                outputArea.append("Risk Level: " + response.getRiskLevel() + "\n");
                outputArea.append("Recommendation: " + response.getRecommendation() + "\n");
                outputArea.append("Computed At: " + response.getComputedAt() + "\n");

            } finally {
                serviceChannel.shutdown();
            }

        } catch (Exception ex) {
            outputArea.append("Error getting health status: " + ex.getMessage() + "\n");
        } finally {
            namingChannel.shutdown();
        }
    }

    private void createWorkOrder() {
        outputArea.setText("");

        ManagedChannel namingChannel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        try {
            NamingServiceGrpc.NamingServiceBlockingStub namingStub =
                    NamingServiceGrpc.newBlockingStub(namingChannel);

            DiscoverServiceRes serviceInfo = namingStub.discoverService(
                    DiscoverServiceReq.newBuilder()
                            .setServiceName("BridgeMaintenanceService")
                            .build()
            );

            if (!serviceInfo.getFound()) {
                outputArea.append("BridgeMaintenanceService not found in Naming Service.\n");
                return;
            }

            ManagedChannel serviceChannel = ManagedChannelBuilder
                    .forAddress(serviceInfo.getHost(), serviceInfo.getPort())
                    .usePlaintext()
                    .build();

            try {
                BridgeMaintenanceServiceGrpc.BridgeMaintenanceServiceBlockingStub maintenanceStub =
                        BridgeMaintenanceServiceGrpc.newBlockingStub(serviceChannel);

                CreateWorkOrderReq request = CreateWorkOrderReq.newBuilder()
                        .setBridgeId("BR-001")
                        .setIssueCode("CRACK_SUSPECTED")
                        .setDescription("Possible crack detected near joint A")
                        .setPriority(Priority.P2_HIGH)
                        .setRequestedBy("GUI")
                        .build();

                CreateWorkOrderRes response = maintenanceStub.createWorkOrder(request);

                outputArea.append("Work Order Created:\n");
                outputArea.append("====================================\n");
                outputArea.append("Service Host: " + serviceInfo.getHost() + "\n");
                outputArea.append("Service Port: " + serviceInfo.getPort() + "\n");
                outputArea.append("Work Order ID: " + response.getWorkOrderId() + "\n");
                outputArea.append("Bridge ID: " + response.getBridgeId() + "\n");
                outputArea.append("Status: " + response.getStatus() + "\n");
                outputArea.append("Created At: " + response.getCreatedAt() + "\n");
                outputArea.append("Message: " + response.getMessage() + "\n");

            } finally {
                serviceChannel.shutdown();
            }

        } catch (Exception ex) {
            outputArea.append("Error creating work order: " + ex.getMessage() + "\n");
        } finally {
            namingChannel.shutdown();
        }
    }

    private void subscribeAlerts() {
        outputArea.setText("Subscribing to live alerts...\n");
        outputArea.append("====================================\n");

        ManagedChannel namingChannel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        try {
            NamingServiceGrpc.NamingServiceBlockingStub namingStub =
                    NamingServiceGrpc.newBlockingStub(namingChannel);

            DiscoverServiceRes serviceInfo = namingStub.discoverService(
                    DiscoverServiceReq.newBuilder()
                            .setServiceName("BridgeHealthService")
                            .build()
            );

            if (!serviceInfo.getFound()) {
                outputArea.append("BridgeHealthService not found in Naming Service.\n");
                return;
            }

            outputArea.append("Service Host: " + serviceInfo.getHost() + "\n");
            outputArea.append("Service Port: " + serviceInfo.getPort() + "\n");
            outputArea.append("------------------------------------\n");

            ManagedChannel serviceChannel = ManagedChannelBuilder
                    .forAddress(serviceInfo.getHost(), serviceInfo.getPort())
                    .usePlaintext()
                    .build();

            BridgeHealthServiceGrpc.BridgeHealthServiceStub asyncStub =
                    BridgeHealthServiceGrpc.newStub(serviceChannel);

            SubscribeAlertsReq request = SubscribeAlertsReq.newBuilder()
                    .setBridgeId("BR-001")
                    .build();

            asyncStub.subscribeAlerts(request, new StreamObserver<SubscribeAlertsRes>() {
                @Override
                public void onNext(SubscribeAlertsRes alert) {
                    SwingUtilities.invokeLater(() -> {
                        outputArea.append("Alert received:\n");
                        outputArea.append("Risk Level: " + alert.getRiskLevel() + "\n");
                        outputArea.append("Alert Code: " + alert.getAlertCode() + "\n");
                        outputArea.append("Details: " + alert.getDetails() + "\n");
                        outputArea.append("Timestamp: " + alert.getTimestamp() + "\n");
                        outputArea.append("------------------------------------\n");
                    });
                }

                @Override
                public void onError(Throwable t) {
                    SwingUtilities.invokeLater(() ->
                            outputArea.append("Error receiving alerts: " + t.getMessage() + "\n"));
                    serviceChannel.shutdown();
                }

                @Override
                public void onCompleted() {
                    SwingUtilities.invokeLater(() ->
                            outputArea.append("Alert stream completed.\n"));
                    serviceChannel.shutdown();
                }
            });

        } catch (Exception ex) {
            outputArea.append("Error subscribing to alerts: " + ex.getMessage() + "\n");
        } finally {
            namingChannel.shutdown();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SmartBridgeGUI gui = new SmartBridgeGUI();
            gui.setVisible(true);
        });
    }
}
