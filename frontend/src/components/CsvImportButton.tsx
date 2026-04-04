import { useState } from 'react';
import { Button, Modal, Upload, Table, Progress, message, Alert } from 'antd';
import { UploadOutlined, InboxOutlined } from '@ant-design/icons';
import apiClient from '../api/client';

interface CsvImportButtonProps {
  endpoint: string; // e.g. '/import/suppliers'
  templateHeaders: string; // CSV header line for the template download
  buttonText?: string;
  onSuccess?: () => void;
}

function parseCsvLine(line: string): string[] {
  const result: string[] = [];
  let inQuotes = false;
  let current = '';
  for (const c of line) {
    if (c === '"') { inQuotes = !inQuotes; }
    else if (c === ',' && !inQuotes) { result.push(current); current = ''; }
    else { current += c; }
  }
  result.push(current);
  return result;
}

export default function CsvImportButton({ endpoint, templateHeaders, buttonText = 'Import CSV', onSuccess }: CsvImportButtonProps) {
  const [open, setOpen] = useState(false);
  const [preview, setPreview] = useState<{ headers: string[]; rows: string[][] } | null>(null);
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState<{ imported: number; skipped: number; errors: string[] } | null>(null);

  const handleFile = (file: File) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target?.result as string;
      const lines = text.split('\n').filter(l => l.trim());
      if (lines.length < 2) {
        message.error('CSV must have a header row and at least one data row');
        return;
      }
      const headers = parseCsvLine(lines[0]);
      const rows = lines.slice(1).map(l => parseCsvLine(l));
      setPreview({ headers, rows });
      setResult(null);
    };
    reader.readAsText(file);
    return false; // prevent upload
  };

  const handleImport = async () => {
    if (!preview) return;
    setImporting(true);
    try {
      // Reconstruct CSV and send as file
      const csvContent = [
        preview.headers.join(','),
        ...preview.rows.map(r => r.join(','))
      ].join('\n');
      const blob = new Blob([csvContent], { type: 'text/csv' });
      const formData = new FormData();
      formData.append('file', blob, 'import.csv');

      const { data } = await apiClient.post(endpoint, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setResult(data);
      message.success(`Imported ${data.imported} records (${data.skipped} skipped)`);
      onSuccess?.();
    } catch (err: unknown) {
      const error = err as { response?: { data?: { error?: string } } };
      message.error(error.response?.data?.error ?? 'Import failed');
    } finally {
      setImporting(false);
    }
  };

  const handleDownloadTemplate = () => {
    const blob = new Blob([templateHeaders + '\n'], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'import-template.csv';
    link.click();
    URL.revokeObjectURL(url);
  };

  const handleClose = () => {
    setOpen(false);
    setPreview(null);
    setResult(null);
  };

  const columns = preview?.headers.map((h, i) => ({
    title: h,
    dataIndex: String(i),
    key: String(i),
    ellipsis: true,
  })) ?? [];

  const dataSource = preview?.rows.map((row, i) => {
    const obj: Record<string, string> = { key: String(i) };
    row.forEach((val, j) => { obj[String(j)] = val; });
    return obj;
  }) ?? [];

  return (
    <>
      <Button icon={<UploadOutlined />} onClick={() => setOpen(true)}>
        {buttonText}
      </Button>
      <Modal
        title="Import CSV"
        open={open}
        onCancel={handleClose}
        width={720}
        footer={preview ? [
          <Button key="template" onClick={handleDownloadTemplate}>Download Template</Button>,
          <Button key="cancel" onClick={handleClose}>Cancel</Button>,
          <Button key="import" type="primary" loading={importing} onClick={handleImport} disabled={!!result}>
            Import {preview.rows.length} rows
          </Button>,
        ] : null}
      >
        {!preview ? (
          <>
            <Alert
              message="CSV Format"
              description={`Expected headers: ${templateHeaders}`}
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />
            <Upload.Dragger
              accept=".csv"
              showUploadList={false}
              beforeUpload={handleFile}
            >
              <p className="ant-upload-drag-icon"><InboxOutlined /></p>
              <p className="ant-upload-text">Click or drag a CSV file here</p>
              <p className="ant-upload-hint">
                <Button size="small" onClick={(e) => { e.stopPropagation(); handleDownloadTemplate(); }}>
                  Download template
                </Button>
              </p>
            </Upload.Dragger>
          </>
        ) : (
          <>
            {result && (
              <Alert
                message={`Import complete: ${result.imported} imported, ${result.skipped} skipped`}
                type={result.imported > 0 ? 'success' : 'warning'}
                showIcon
                style={{ marginBottom: 16 }}
              />
            )}
            {importing && <Progress percent={100} status="active" style={{ marginBottom: 16 }} />}
            <Table
              columns={columns}
              dataSource={dataSource}
              size="small"
              scroll={{ x: 'max-content', y: 300 }}
              pagination={false}
            />
          </>
        )}
      </Modal>
    </>
  );
}
