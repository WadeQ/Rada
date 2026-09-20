import SwiftUI

struct HelpView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Roadside help").font(.system(size: 17, weight: .semibold)).foregroundColor(.radaTextPrimary)
            Text("Free for every driver")
                .font(.system(size: 10.5, weight: .bold))
                .foregroundColor(.radaAccentText)
                .padding(.horizontal, 9).padding(.vertical, 3)
                .background(RoundedRectangle(cornerRadius: 8).fill(Color.radaAccentText.opacity(0.14)))
                .padding(.top, 6)
                .padding(.bottom, 10)

            ScrollView {
                sectionView("Emergency", RadaMockData.roadsideContacts.filter { $0.category == .emergency })
                sectionView("Tow companies nearby", RadaMockData.roadsideContacts.filter { $0.category == .tow })
                sectionView("Recommended mechanics", RadaMockData.roadsideContacts.filter { $0.category == .mechanic })
            }

            Spacer().frame(height: bottomBarClearance)
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 20)
    }

    private func sectionView(_ title: String, _ contacts: [RadaMockData.Contact]) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title).font(.system(size: 12.5, weight: .semibold)).foregroundColor(.radaTextPrimary)
                .padding(.vertical, 6)
            ForEach(contacts, id: \.name) { contact in
                contactRow(contact)
            }
        }
    }

    private func contactRow(_ contact: RadaMockData.Contact) -> some View {
        HStack {
            Circle().fill(Color.radaInk2).frame(width: 32, height: 32)
                .overlay(Image(systemName: "shield.fill").font(.system(size: 13)).foregroundColor(.radaTextSecondary))
            VStack(alignment: .leading) {
                Text(contact.name).font(.system(size: 12.5, weight: .semibold)).foregroundColor(.radaTextPrimary)
                Text(contact.meta).font(.system(size: 10.5)).foregroundColor(.radaTextMuted)
            }
            Spacer()
            Button(action: {
                // NEEDS VERIFICATION: `tel:` URLs only dial on a real device, not the
                // Simulator, and require the number to contain no spaces.
                if let url = URL(string: "tel://\(contact.phone)") {
                    openURLExternally(url)
                }
            }) {
                Circle().fill(Color.radaAccent).frame(width: 32, height: 32)
                    .overlay(Image(systemName: "phone.fill").font(.system(size: 13)).foregroundColor(.radaAccentInk))
            }
        }
        .padding(.vertical, 8)
    }
}
