package com.example.a534researchandroidapp;

// From https://github.com/PunchThrough/ble-starter-android/commit/e3ffec7f0c9d1851af007059869d55e526805413?diff=unified&w=0
import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {

    private static List<ScanResult> items;
    private OnItemClickListener onClickListener;

    public ScanResultAdapter(List<ScanResult> items, OnItemClickListener onClickListener) {
        this.items = items;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_scan_result, parent, false);
        return new ViewHolder(view, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onClick(ScanResult device);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView deviceName;
        private TextView macAddress;
        private TextView signalStrength;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onClickListener) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            macAddress = itemView.findViewById(R.id.mac_address);
            signalStrength = itemView.findViewById(R.id.signal_strength);

            itemView.setOnClickListener(v -> {
                if (onClickListener != null) {
                    onClickListener.onClick(items.get(getAdapterPosition()));
                }
            });
        }

        @SuppressLint("MissingPermission")
        public void bind(ScanResult result) {
            deviceName.setText(result.getDevice().getName() != null ? result.getDevice().getName() : "Unnamed");
            macAddress.setText(result.getDevice().getAddress());
            signalStrength.setText(result.getRssi() + " dBm");
        }
    }
}
