import SwiftUI

struct ReviewView: View {
    var body: some View {
        VStack(spacing: 20) {
            Text("2/3 Senses")
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            Text("serendipity")
                .font(.system(size: 36, weight: .bold))
            
            Text("noun")
                .font(.title3)
                .foregroundColor(.blue)
            
            Text("the occurrence and development of events by chance in a happy or beneficial way.")
                .font(.body)
                .multilineTextAlignment(.center)
                .padding()
            
            Spacer()
            
            HStack(spacing: 20) {
                Button(action: {}) {
                    Text("Again")
                        .frame(minWidth: 80)
                        .padding()
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                
                Button(action: {}) {
                    Text("Hard")
                        .frame(minWidth: 80)
                        .padding()
                        .background(Color.orange)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                
                Button(action: {}) {
                    Text("Good")
                        .frame(minWidth: 80)
                        .padding()
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
            .padding(.bottom, 40)
        }
        .padding()
    }
}
