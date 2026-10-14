package ar.com.cinemundo.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.window.OnBackInvokedDispatcher;

public class MainActivity extends Activity {

    // La dirección que abre la app
    private static final String URL_INICIO = "https://cinemundo.com.ar/";
    private static final String DOMINIO = "cinemundo.com.ar";

    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        setContentView(web);

        // En Android 15+ la pantalla va de borde a borde: dejamos lugar para las barras del sistema
        if (Build.VERSION.SDK_INT >= 30) {
            web.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets b = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(b.left, b.top, b.right, b.bottom);
                return WindowInsets.CONSUMED;
            });
        }

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri uri = req.getUrl();
                String host = uri.getHost();
                String scheme = uri.getScheme();
                boolean esWeb = "http".equals(scheme) || "https".equals(scheme);
                // Las páginas de cinemundo se quedan en la app
                if (esWeb && host != null && host.endsWith(DOMINIO)) {
                    return false;
                }
                // Todo lo demás (YouTube, mail, WhatsApp, otros sitios) se abre afuera
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    String html = "<html><body style='font-family:sans-serif;text-align:center;"
                            + "padding:3em 1em;background:#111;color:#eee'>"
                            + "<h2>Sin conexión</h2><p>No se pudo abrir Cine Mundo.<br>"
                            + "Revisá internet y probá de nuevo.</p>"
                            + "<p><a style='color:#f5c518' href='" + URL_INICIO + "'>Reintentar</a></p>"
                            + "</body></html>";
                    view.loadDataWithBaseURL(URL_INICIO, html, "text/html", "UTF-8", URL_INICIO);
                }
            }
        });

        // Botón "atrás": vuelve a la página anterior antes de cerrar la app
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::atras);
        }

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(URL_INICIO);
        }
    }

    private void atras() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            finish();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        atras();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }
}
