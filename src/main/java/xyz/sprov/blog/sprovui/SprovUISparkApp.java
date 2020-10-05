package xyz.sprov.blog.sprovui;

import xyz.sprov.blog.sprovui.util.Config;
import xyz.sprov.blog.sprovui.util.SparkUtil;

import static spark.Spark.*;
import static xyz.sprov.blog.sprovui.util.Context.*;

public class SprovUISparkApp {

    static {
        System.setProperty("file.encoding", "UTF-8");
    }

    public static void main(String[] args) {


        
        long start = System.currentTimeMillis();
        int port  = Config.getPort();
        port(port);
        threadPool(4, 1, 60000);

        initExceptionHandler(e -> {
            System.err.println("sprov-ui 启动失败：" + e.getMessage());
            System.exit(1);
        });

        exception(Exception.class, (e, request, response) -> System.out.println(e.getMessage()));

        staticFiles.location("/static");
        staticFiles.expireTime(3600 * 24 * 30 * 6);

        before("", encodingFilter);
        before("/*", encodingFilter);

        get("/", baseRoute.index());
        post("/login", baseRoute.login(), jsonTransformer);

        get("/robots.txt", baseRoute.robots());

        path("/v2ray", () -> {
            before("", loginFilter);
            before("/*", loginFilter);

            get("", SparkUtil.view("/v2ray/index"));
            get("/", SparkUtil.view("/v2ray/index"));
            get("/accounts", SparkUtil.view("/v2ray/accounts"));
            get("/accounts/", SparkUtil.view("/v2ray/accounts"));
            get("/clients", SparkUtil.view("/v2ray/clients"));
            get("/clients/", SparkUtil.view("/v2ray/clients"));

            post("/status", v2rayRoute.status(), jsonTransformer);
            post("/start", v2rayRoute.start(), jsonTransformer);
            post("/restart", v2rayRoute.restart(), jsonTransformer);
            post("/stop", v2rayRoute.stop(), jsonTransformer);
            post("/config", v2rayRoute.config(), jsonTransformer);

            path("/inbound", () -> {
                post("/add", inboundsRoute.add(), jsonTransformer);
                post("/edit", inboundsRoute.edit(), jsonTransformer);
                post("/del", inboundsRoute.del(), jsonTransformer);

                post("/openTraffic", inboundsRoute.openTraffic(), jsonTransformer);
                post("/resetTraffic", inboundsRoute.resetTraffic(), jsonTransformer);
                post("/resetAllTraffic", inboundsRoute.resetAllTraffic(), jsonTransformer);

                post("/enable", inboundsRoute.enable(), jsonTransformer);
                post("/disable", inboundsRoute.disable(), jsonTransformer);
                post("/delDisabled", inboundsRoute.delDisabled(), jsonTransformer);
            });
        });

        path("/server", () -> {
            before("", loginFilter);
            before("/*", loginFilter);

            post("/status", serverRoute.status(), jsonTransformer);
        });

        path("/sprov-ui", () -> {
            before("", loginFilter);
            before("/*", loginFilter);

            post("/isLastVersion", sprovUIController.isLastVersion(), jsonTransformer);
            post("/update", sprovUIController.update(), jsonTransformer);
            post("/restart", sprovUIController.restart(), jsonTransformer);
        });
        awaitInitialization();
        long end = System.currentTimeMillis();
        System.out.println("sprov-ui 启动成功，耗时 " + (end - start) + " ms，面板监听端口为 " + port);
    }

}
