import SwiftUI
import UIKit
import ComposeApp
import OneSignalFramework

/// App de OneSignal de Astro Pollo (no es secreta: va dentro de la app).
private let oneSignalAppId = "f86a2ee4-a10b-4a86-a063-151be6845bce"

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        OneSignal.initialize(oneSignalAppId, withLaunchOptions: launchOptions)

        // el código compartido (Kotlin) lee el id de suscripción y pide el permiso
        Notificaciones.shared.puente = PuenteOneSignal()
        Notificaciones.shared.pedirPermiso()
        return true
    }
}

/// Implementa la interfaz de Kotlin `PuenteNotificaciones` con el SDK de OneSignal.
final class PuenteOneSignal: NSObject, PuenteNotificaciones {
    func idSuscripcion() -> String? {
        OneSignal.User.pushSubscription.id
    }

    func iniciarSesion(externalId: String) {
        OneSignal.login(externalId)
    }

    func cerrarSesion() {
        OneSignal.logout()
    }

    func pedirPermiso() {
        // solo muestra el aviso del sistema si el usuario aún no ha respondido
        DispatchQueue.main.async {
            OneSignal.Notifications.requestPermission({ _ in }, fallbackToSettings: false)
        }
    }
}
