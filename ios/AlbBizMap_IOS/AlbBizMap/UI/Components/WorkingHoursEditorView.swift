// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Shared 7-day working-hours editor, used by AddBusinessScreen and
// EditBusinessScreen. Mirrors Android's WorkingHoursEditor composable: one
// row per day, each with an Open/Close toggle and, when open, a pair of
// compact time pickers.
public struct WorkingHoursEditorView: View {
    @Binding var hours: [String: String]
    private let days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]

    public init(hours: Binding<[String: String]>) {
        self._hours = hours
    }

    public var body: some View {
        VStack(spacing: 8) {
            ForEach(days, id: \.self) { day in
                DayRow(day: day, hours: $hours)
            }
        }
    }
}

private struct DayRow: View {
    @Environment(\.appStrings) private var strings
    let day: String
    @Binding var hours: [String: String]

    private var isClosed: Bool {
        hours["\(day)_closed"] == "true"
    }

    private var openTimeBinding: Binding<Date> {
        Binding(
            get: { Self.parseTime(hours["\(day)_open"] ?? "09:00") },
            set: { hours["\(day)_open"] = Self.formatTime($0) }
        )
    }

    private var closeTimeBinding: Binding<Date> {
        Binding(
            get: { Self.parseTime(hours["\(day)_close"] ?? "18:00") },
            set: { hours["\(day)_close"] = Self.formatTime($0) }
        )
    }

    var body: some View {
        HStack(spacing: 8) {
            Text(day)
                .font(.system(size: 13, weight: .bold))
                .frame(width: 36, alignment: .leading)

            if isClosed {
                Text(strings.closedLabel)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(.meTontRed)
                    .frame(maxWidth: .infinity, alignment: .leading)
            } else {
                HStack(spacing: 4) {
                    Image(systemName: "clock").font(.system(size: 12)).foregroundColor(.meTontRed)
                    DatePicker("", selection: openTimeBinding, displayedComponents: .hourAndMinute)
                        .labelsHidden()
                        .datePickerStyle(.compact)
                    Text("-").foregroundColor(.meTontGrey).fontWeight(.bold)
                    DatePicker("", selection: closeTimeBinding, displayedComponents: .hourAndMinute)
                        .labelsHidden()
                        .datePickerStyle(.compact)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }

            Button(action: {
                hours["\(day)_closed"] = isClosed ? "false" : "true"
            }) {
                Text(isClosed ? strings.hoursOpenLabel : strings.hoursCloseLabel)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.meTontRed)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color(red: 0xF5/255.0, green: 0xF5/255.0, blue: 0xF5/255.0))
        .cornerRadius(10)
    }

    private static func parseTime(_ time: String) -> Date {
        let parts = time.split(separator: ":")
        let hour = parts.count > 0 ? Int(parts[0]) ?? 9 : 9
        let minute = parts.count > 1 ? Int(parts[1]) ?? 0 : 0
        var comps = DateComponents()
        comps.hour = hour
        comps.minute = minute
        return Calendar.current.date(from: comps) ?? Date()
    }

    private static func formatTime(_ date: Date) -> String {
        let comps = Calendar.current.dateComponents([.hour, .minute], from: date)
        return String(format: "%02d:%02d", comps.hour ?? 9, comps.minute ?? 0)
    }
}
