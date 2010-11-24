/**
 * ManagementLicenseAdministrationLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package iControl;

public class ManagementLicenseAdministrationLocator extends org.apache.axis.client.Service implements iControl.ManagementLicenseAdministration {

/**
 * The LicenseAdministration interface exposes methods that enable
 * you to authorize the system,
 *  either manually or in an automated fashion.  This interface allows
 * you to generate license
 *  files, install previously generated licenses, and view other licensing
 * characteristics.
 */

    public ManagementLicenseAdministrationLocator() {
    }


    public ManagementLicenseAdministrationLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ManagementLicenseAdministrationLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ManagementLicenseAdministrationPort
    private java.lang.String ManagementLicenseAdministrationPort_address = "https://url_to_service";

    public java.lang.String getManagementLicenseAdministrationPortAddress() {
        return ManagementLicenseAdministrationPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ManagementLicenseAdministrationPortWSDDServiceName = "Management.LicenseAdministrationPort";

    public java.lang.String getManagementLicenseAdministrationPortWSDDServiceName() {
        return ManagementLicenseAdministrationPortWSDDServiceName;
    }

    public void setManagementLicenseAdministrationPortWSDDServiceName(java.lang.String name) {
        ManagementLicenseAdministrationPortWSDDServiceName = name;
    }

    public iControl.ManagementLicenseAdministrationPortType getManagementLicenseAdministrationPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ManagementLicenseAdministrationPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getManagementLicenseAdministrationPort(endpoint);
    }

    public iControl.ManagementLicenseAdministrationPortType getManagementLicenseAdministrationPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            iControl.ManagementLicenseAdministrationBindingStub _stub = new iControl.ManagementLicenseAdministrationBindingStub(portAddress, this);
            _stub.setPortName(getManagementLicenseAdministrationPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setManagementLicenseAdministrationPortEndpointAddress(java.lang.String address) {
        ManagementLicenseAdministrationPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (iControl.ManagementLicenseAdministrationPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                iControl.ManagementLicenseAdministrationBindingStub _stub = new iControl.ManagementLicenseAdministrationBindingStub(new java.net.URL(ManagementLicenseAdministrationPort_address), this);
                _stub.setPortName(getManagementLicenseAdministrationPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("Management.LicenseAdministrationPort".equals(inputPortName)) {
            return getManagementLicenseAdministrationPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:iControl", "Management.LicenseAdministration");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:iControl", "Management.LicenseAdministrationPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ManagementLicenseAdministrationPort".equals(portName)) {
            setManagementLicenseAdministrationPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}