package sap.prd.cmintegration.cli;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;
import com.sap.cmclient.http.CMODataAbapClient;

public class ABAPTransportTest extends CMABAPTestBase {

    Capture<Map<String, Object>> transportMap = Capture.newInstance();

    AbapClientFactory setupGetTransportMock(Transport t) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.getTransport(anyString())).andReturn(t);
        expect(factoryMock.newClient(capture(host), capture(user), capture(password))).andReturn(clientMock);

        replay(factoryMock, clientMock);

        return factoryMock;
    }

    AbapClientFactory setupCreateTransportMock(Transport result) throws Exception {

        AbapClientFactory factoryMock = createMock(AbapClientFactory.class);
        CMODataAbapClient clientMock = createMock(CMODataAbapClient.class);

        expect(clientMock.createTransport(capture(transportMap))).andReturn(result);
        expect(factoryMock.newClient(capture(host), capture(user), capture(password))).andReturn(clientMock);

        replay(factoryMock, clientMock);

        return factoryMock;
    }


    @After
    public void tearDown() throws Exception {
        System.setOut(oldOut);
        setMock(null);
    }

    @Test
    public void testServerCoordinatesStraightForward() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        setMock(setupGetTransportMock(new Transport(m)));

        GetTransportOwnerABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "-tID", "999"});

        assertThat(host.getValue(), is(equalTo("http://example.org:8000/endpoint")));
        assertThat(user.getValue(), is(equalTo("me")));
        assertThat(password.getValue(), is(equalTo("openSesame")));
    }

    @Test
    public void testUserNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: u");

        GetTransportOwnerABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "-tID", "999"});
    }

    @Test
    public void testEndpointNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: e");

        GetTransportOwnerABAP.main(new String[]
                {       "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "-tID", "999"});
    }

    @Test
    public void testPasswordNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: p");

        GetTransportOwnerABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-t", "ABAP",
                        "-tID", "999"});
    }

    @Test
    public void testTransportIdNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: tID");

        GetTransportOwnerABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP"});
    }

    @Test
    public void testBackendTypeNotProvided() throws Exception {

        thrown.expect(MissingOptionException.class);
        thrown.expectMessage("Missing required option: t");

        GetTransportOwnerABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-tID", "999"});
    }

    @Test
    public void testInvalidBackendTypeProvided() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No enum constant");
        thrown.expectMessage("XXX");

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "XXX",
                        "-tID", "999"});
    }

    @Test
    public void test_InterstingFinding_TransportIDBeforeBackendTypeDoesNotWork() throws Exception {

        //
        // In this case "-tID" is understaod like "-t ID".
        //
        // Since we document that the generic options needs to be provided first - and with that
        // before the subcommand and the subcommand options this does not hurt.
        //

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No enum constant");
        thrown.expectMessage("ID");

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-tID", "999",
                        "-t", "XXX"});
    }

    @Test
    public void testGetTransportOwnerStraightForward() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Owner", "Admin");
        setMock(setupGetTransportMock(new Transport(m)));

        GetTransportOwnerABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "-tID", "999"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("Admin")));
    }

    @Test
    public void testGetTransportDescriptionStraightForward() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Description", "desc");
        setMock(setupGetTransportMock(new Transport(m)));

        GetTransportDescriptionABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "-tID", "999"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("desc")));
    }

    @Test
    public void testLookupNotExistingTransportRaisesTransportNotFoundException() throws Exception {

        thrown.expect(TransportNotFoundException.class);
        thrown.expectMessage("Transport '999' not found.");

        setMock(setupGetTransportMock(null));

        GetTransportOwnerABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "-tID", "999"});
    }

    @Test
    public void testUnmatchedTransportRaisesCMCommandLineException() throws Exception {

        thrown.expect(CMCommandLineException.class);
        thrown.expectMessage("TransportId of resolved transport ('998') does not match requested transport id ('999').");

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "998");

        setMock(setupGetTransportMock(new Transport(m)));

        GetTransportOwnerABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "-tID", "999"});
    }

    @Test
    public void testCreateTransport() throws Exception {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        setMock(setupCreateTransportMock(new Transport(m)));

        CreateTransportABAP.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "-tt", "K",
                        "-d", "desc",
                        "-ts", "A5X"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("999")));

        Map<String, Object> tMap = transportMap.getValue();

        //
        // We test if the map used for creating the tranport on the backend
        // is fine. Testing if the transport instance finally returned matches
        // is tested below.
        //

        // Id must not be provided in the map. Id is provided from the
        // backend.
        assertThat(tMap.get("Id"), is(equalTo("")));
        assertThat(tMap.get("Description"), is(equalTo("desc")));
        assertThat(tMap.get("TarSystem"), is(equalTo("A5X")));
        assertThat(tMap.get("Type"), is(equalTo("K")));
    }

    @Test
    public void testCreateTransportFromMap() {

        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Description", "desc");
        m.put("TarSystem", "A5X");
        m.put("Type", "K");

        Transport t = new Transport(m);

        assertThat(t.getTransportID(), is(equalTo("999")));
        assertThat(t.getDescription(), is(equalTo("desc")));
        assertThat(t.getTargetSystem(), is(equalTo("A5X")));
        assertThat(t.getType(), is(equalTo("K")));
    }

    @Test
    public void testTransportWithoutIdRaisesIllegalArgumentException() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Key 'Id' must not be null");

        // The map created below does not have an id member
        new Transport(Maps.newHashMap());
    }
}