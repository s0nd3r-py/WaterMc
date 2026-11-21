package com.destroystokyo.paper;

import static io.papermc.paper.ServerBuildInfo.StringRepresentation.VERSION_SIMPLE;

// Gale start - branding changes - version fetcher
/**
 * The original version fetcher for Paper. Most of the original content of this class has been moved to
 * {@link me.s0nd3r.watermc.tuning.version.AbstractPaperVersionFetcher}.
 */
public class PaperVersionFetcher extends me.s0nd3r.watermc.tuning.version.AbstractPaperVersionFetcher {

    public static final String DOWNLOAD_PAGE = "https://papermc.io/downloads/paper";
    public static final String REPOSITORY = "PaperMC/Paper";
    public static final String API_URL = "https://fill.papermc.io/v3/projects/paper/versions/" + PaperVersionFetcher.BUILD_INFO.minecraftVersionId() + "/builds";
    public static final String USER_AGENT = BUILD_INFO.brandName() + "/" + BUILD_INFO.asString(VERSION_SIMPLE) + " (https://papermc.io)";

    public PaperVersionFetcher() {
        super(
            DOWNLOAD_PAGE,
            "PaperMC",
            "Paper",
            "PaperMC",
            "Paper",
            API_URL,
            USER_AGENT,
            ApiType.FILLV3
        );
    }
    // Gale end - branding changes - version fetcher
}
