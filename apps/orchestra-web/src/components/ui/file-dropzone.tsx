import React, { useCallback } from 'react';
import { useDropzone, Accept } from 'react-dropzone';
import { UploadCloud, File as FileIcon, X } from 'lucide-react';
import { cn } from '../../lib/utils';
import { Button } from './button';

interface FileDropzoneProps {
  onFileSelect: (file: File | null) => void;
  selectedFile: File | null;
  accept?: Accept;
  label?: string;
  className?: string;
}

export const FileDropzone: React.FC<FileDropzoneProps> = ({
  onFileSelect,
  selectedFile,
  accept,
  label = "Drag & drop a file here, or click to select",
  className,
}) => {
  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles.length > 0) {
      onFileSelect(acceptedFiles[0]);
    }
  }, [onFileSelect]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept,
    multiple: false,
  });

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    onFileSelect(null);
  };

  if (selectedFile) {
    return (
      <div className={cn("flex items-center justify-between rounded-md border bg-muted/30 p-3", className)}>
        <div className="flex items-center gap-3 overflow-hidden">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded bg-background border">
            <FileIcon className="h-5 w-5 text-violet-600" />
          </div>
          <div className="flex flex-col overflow-hidden">
            <span className="truncate text-sm font-medium">{selectedFile.name}</span>
            <span className="text-xs text-muted-foreground">{(selectedFile.size / 1024).toFixed(1)} KB</span>
          </div>
        </div>
        <Button variant="ghost" size="icon" onClick={handleRemove} className="shrink-0 text-muted-foreground hover:text-destructive">
          <X className="h-4 w-4" />
        </Button>
      </div>
    );
  }

  return (
    <div
      {...getRootProps()}
      className={cn(
        "flex flex-col items-center justify-center rounded-md border-2 border-dashed p-6 text-center transition-colors cursor-pointer hover:bg-muted/20",
        isDragActive ? "border-violet-500 bg-violet-50/50" : "border-muted-foreground/25",
        className
      )}
    >
      <input {...getInputProps()} />
      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted/50 mb-3">
        <UploadCloud className={cn("h-6 w-6", isDragActive ? "text-violet-600" : "text-muted-foreground")} />
      </div>
      <p className="text-sm font-medium text-foreground">{isDragActive ? "Drop it here!" : label}</p>
      <p className="text-xs text-muted-foreground mt-1">Max file size: 10MB</p>
    </div>
  );
};


