# Peerless PACS Connector

## Overview
`com.nairobi.Server` is a standalone socket listener that accepts HL7 ORU/ORC report payloads from the Peerless modality bridge and forwards them to the MedNet Lab web service. The class lives in `src/com/peerless/Server.java` and is configured through the colocated `pacs-connector.properties`.

## Runtime Flow
1. Load configuration from `/D:/PACS_CONNECTOR/pacs-connector.properties` (see the template checked into this folder).
2. Open a `ServerSocket` on `pacs.port` and wait for incoming TCP clients.
3. Assemble the message until the HL7 end-of-message delimiter `0x1C` is encountered.
4. Normalize the raw frame via `addSegmentSeparators(..)` which inserts `\r` before every known segment (`PID`, `PV1`, `ORC`, etc.) so downstream systems always receive a standards-compliant document.
5. Post the normalized message (Base64 encoded) to `pacs.updateReport.url` with the MedNet OAuth token headers.
6. Relay the Base64-decoded ACK response back to the modality, wrapped with the standard `0x0B ... 0x1C 0x0D` MLLP delimiters.

## Configuration Keys (`pacs-connector.properties`)
- `pacs.host` / `pacs.port`: IP/port the socket binds to (defaults in repo: `192.168.2.81:6161`).
- `pacs.updateReport.url`: MedNet endpoint for `updatePatientReport`.
- `pacs.sendingApp`, `pacs.sendingFac`, `pacs.recApp`, `pacs.recFac`, `pacs.controlID`: HL7 header defaults for fallback ACK creation when the upstream call fails.
- OAuth and logged-in IDs are currently hard-coded in `Server.java`; update before production use.

## Error Handling
- When HL7 parsing or the web-service call fails, `Server` fabricates an `ACK|AE` message using the config-specified application/facility values. This fallback ensures the modality always receives an ACK frame even on errors.
- Socket-level exceptions are logged through `LabConnectUtil`.

## Extending / Future Work
- Externalize the OAuth token and logged-in user ID to the properties file.
- Add retries or circuit-breaking around the MedNet POST call (current timeout: 60s receive, 30s connect).
- Provide automated tests that feed sample HL7 payloads (with and without explicit `\r`) through `addSegmentSeparators(..)` to guard against regressions.

## Quick Test Checklist
- Start the server (`java com.nairobi.Server`) after copying `pacs-connector.properties` to `D:/PACS_CONNECTOR/`.
- Use `netcat` or `SimpleTCPClient` to send an HL7 message terminated with `0x1C0D`. Confirm the server logs the normalized payload and that the modality receives an ACK containing the message control ID returned by MedNet.


