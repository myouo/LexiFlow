import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            DashboardView()
                .tabItem {
                    Label("Dashboard", systemImage: "chart.bar.doc.horizontal")
                }
            ReviewView()
                .tabItem {
                    Label("Review", systemImage: "book.fill")
                }
            Text("Settings Placeholder")
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
        }
    }
}
