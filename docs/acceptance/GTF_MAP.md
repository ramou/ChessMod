# GameTest implementation map

The feature files are the **canonical behavioural specifications**. GTF is an optional way to execute particular step IDs; no GTF test has been verified or registered by this patch. Test registration, discovery count, exit status and tested assertions must be recorded per branch and commit before binding a GTF step.

On older versions without usable GTF support, perform steps manually. Do not alter the acceptance matrix automatically based on Gradle success.

### QBOARD
- Feature: [QBOARD](QBOARD.md). Candidate step IDs: `QBOARD-03`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### QLINK
- Feature: [QLINK](QLINK.md). Candidate step IDs: `QLINK-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### QSYNC
- Feature: [QSYNC](QSYNC.md). Candidate step IDs: `QSYNC-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### QLIFE
- Feature: [QLIFE](QLIFE.md). Candidate step IDs: `QLIFE-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### UPDATE
- Feature: [UPDATE](UPDATE.md). Candidate step IDs: `UPDATE-03`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### IMPORT
- Feature: [IMPORT](IMPORT.md). Candidate step IDs: `IMPORT-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### WOOD
- Feature: [WOOD](WOOD.md). Candidate step IDs: `WOOD-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### RULES
- Feature: [RULES](RULES.md). Candidate step IDs: `RULES-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### STUBS
- Feature: [STUBS](STUBS.md). Candidate step IDs: `STUBS-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### WRENCH
- Feature: [WRENCH](WRENCH.md). Candidate step IDs: `WRENCH-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### GUI
- Feature: [GUI](GUI.md). Candidate step IDs: `GUI-05`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### AUDIO
- Feature: [AUDIO](AUDIO.md). Candidate step IDs: `AUDIO-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### SAVE
- Feature: [SAVE](SAVE.md). Candidate step IDs: `SAVE-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### MULTI
- Feature: [MULTI](MULTI.md). Candidate step IDs: `MULTI-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### FRCA
- Feature: [FRCA](FRCA.md). Candidate step IDs: `FRCA-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

### ZHCN
- Feature: [ZHCN](ZHCN.md). Candidate step IDs: `ZHCN-05`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4). Static JSON/resource checks are not GTF acceptance evidence.

### ELO
- Feature: [ELO](ELO.md). Candidate step IDs: `ELO-04`.
- Verified GTF implementations: **none yet** (1.21, 1.20.1, 1.19.4–1.18.2, 1.17.1–1.14.4).

**When a branch gets GTF:** replace its `none yet` with a link to that branch’s implementation at a pinned SHA (`https://github.com/ramou/ChessMod/blob/<sha>/project/...`), the registered GameTest ID, template/fixture reference and the step IDs it covers. A directory or Gradle run target alone does not constitute an implementation.

Record in the run result: execution command, successful discovery count, failing-test gate evidence and output. For the 1.18+ branches, `runGameTestServer` is a reasonable candidate; older branches need a capability assessment, not an assumed `N/A`.

**Priority:** QSYNC and QLIFE, then SAVE and MULTI, then RULES and QLINK. They exercise persistence and server authority which Java-only JUnit does not establish.
