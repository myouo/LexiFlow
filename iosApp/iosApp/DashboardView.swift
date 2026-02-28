import SwiftUI

struct DashboardView: View {
    var body: some View {
        NavigationView {
            VStack(alignment: .leading, spacing: 20) {
                Text("1-Year Learning Heatmap")
                    .font(.title2)
                    .fontWeight(.bold)
                    .padding(.horizontal)
                
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 4) {
                        ForEach(0..<52, id: \.self) { col in
                            VStack(spacing: 4) {
                                ForEach(0..<7, id: \.self) { row in
                                    RoundedRectangle(cornerRadius: 2)
                                        .fill(colorForLevel(Int.random(in: 0...4)))
                                        .frame(width: 12, height: 12)
                                }
                            }
                        }
                    }
                    .padding()
                }
                Spacer()
            }
            .navigationTitle("LexiFlow")
        }
    }
    
    func colorForLevel(_ level: Int) -> Color {
        switch level {
        case 1: return Color(red: 0.61, green: 0.91, blue: 0.66)
        case 2: return Color(red: 0.25, green: 0.77, blue: 0.39)
        case 3: return Color(red: 0.19, green: 0.63, blue: 0.31)
        case 4: return Color(red: 0.13, green: 0.43, blue: 0.22)
        default: return Color(red: 0.92, green: 0.93, blue: 0.94)
        }
    }
}
